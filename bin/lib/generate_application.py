#!/usr/bin/env python3
"""Build a TE9/PE3 application payload from CCD case data and fill the PDF template."""

from __future__ import annotations

import argparse
import json
import random
import re
import sys
from datetime import date, timedelta
from io import BytesIO
from pathlib import Path
from typing import Any

try:
    from pypdf import PdfReader, PdfWriter
except ImportError:  # pragma: no cover
    print(
        "Missing dependency: pypdf. Install with: python3 -m pip install pypdf reportlab",
        file=sys.stderr,
    )
    sys.exit(1)

try:
    from reportlab.lib.colors import black
    from reportlab.pdfgen import canvas
except ImportError:  # pragma: no cover
    print(
        "Missing dependency: reportlab. Install with: python3 -m pip install pypdf reportlab",
        file=sys.stderr,
    )
    sys.exit(1)

TITLES = ("Mr", "Mrs", "Miss", "Ms", "Other")
LOCATIONS = (
    "High Street car park",
    "Market Square",
    "Station Road",
    "Council offices forecourt",
    "Bus lane, Church Street",
    "Double yellow lines, Mill Lane",
)
COMPANIES = (
    "ACME LOGISTICS LTD",
    "NORTHSTAR FLEET SERVICES",
    "RIVERSIDE PARKING LTD",
    "",
    "",
)
HOW_PAID = ("Cash", "Cheque", "Debit card", "Credit card")
PAID_TO = (
    "Local authority",
    "Charging Authority",
    "Council payment office",
    "Bailiff",
)

TE9_DECLARATIONS = (
    "didNotReceivePcn",
    "madeRepresentationsNoRejection",
    "appealedToAdjudicator",
    "paidInFull",
)
PE3_DECLARATIONS = (
    "didNotReceiveNotice",
    "madeRepresentationsNoRejection",
    "appealedNoResponse",
)

POSTCODE_RE = re.compile(
    r"^[A-Z]{1,2}\d[A-Z\d]?\s*\d[A-Z]{2}$",
    re.IGNORECASE,
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--case-json", required=True, help="Path to CCD case JSON or '-' for stdin")
    parser.add_argument("--type", required=True, choices=("inTime", "outOfTime"))
    parser.add_argument("--form", required=True, choices=("TE9", "PE3"))
    parser.add_argument("--template", required=True, type=Path)
    parser.add_argument("--out-pdf", required=True, type=Path)
    parser.add_argument("--out-payload", required=True, type=Path)
    parser.add_argument("--seed", type=int, default=None)
    return parser.parse_args()


def load_case(path: str) -> dict[str, Any]:
    if path == "-":
        raw = sys.stdin.read()
    else:
        raw = Path(path).read_text(encoding="utf-8")
    case = json.loads(raw)
    data = case.get("data")
    if not isinstance(data, dict):
        raise SystemExit("CCD case JSON did not contain a data object")
    return data


def looks_like_postcode(value: str | None) -> bool:
    return bool(value and POSTCODE_RE.match(value.strip()))


def parse_certificate_date(value: str | None) -> date | None:
    if not value or not re.fullmatch(r"\d{6}", value):
        return None
    year = 2000 + int(value[0:2])
    month = int(value[2:4])
    day = int(value[4:6])
    try:
        return date(year, month, day)
    except ValueError:
        return None


def iso(d: date | None) -> str | None:
    return d.isoformat() if d else None


def split_postcode(postcode: str) -> tuple[str, str]:
    parts = postcode.strip().upper().split()
    if len(parts) >= 2:
        return parts[0], parts[1]
    if len(postcode) > 3:
        return postcode[:-3].strip(), postcode[-3:].strip()
    return postcode, ""


def build_application(case_data: dict[str, Any], timeliness: str, form: str, rng: random.Random) -> dict[str, Any]:
    name = (case_data.get("respondentDetails1") or "ALEX EXAMPLE").strip()
    line2 = (case_data.get("respondentDetails2") or "").strip()
    line3 = (case_data.get("respondentDetails3") or "").strip()
    line4 = (case_data.get("respondentDetails4") or "").strip()
    line5 = (case_data.get("respondentDetails5") or "").strip()
    line6 = (case_data.get("respondentDetails6") or "").strip()

    address_parts = [p for p in (line2, line3) if p]
    postcode = None
    if looks_like_postcode(line4):
        postcode = line4.upper()
    else:
        if line4:
            address_parts.append(line4)
        for candidate in (line5, line6):
            if looks_like_postcode(candidate):
                postcode = candidate.upper()
            elif candidate:
                address_parts.append(candidate)
        if postcode is None:
            postcode = rng.choice(("SW1A 1AA", "NN1 2LH", "M1 1AE", "B1 1BB"))

    certificate_date = parse_certificate_date(case_data.get("dateChargeCertificateServed"))
    if certificate_date:
        date_of_contravention = certificate_date - timedelta(days=rng.randint(14, 90))
    else:
        date_of_contravention = date.today() - timedelta(days=rng.randint(30, 180))

    date_received = date.today() - timedelta(days=rng.randint(0, 14))
    title = rng.choice(TITLES)
    company = rng.choice(COMPANIES)

    if form == "TE9":
        roll = rng.random()
        if roll < 0.1:
            selected_declarations: list[str] = []
        elif roll < 0.2:
            selected_declarations = rng.sample(list(TE9_DECLARATIONS), k=2)
        else:
            selected_declarations = [rng.choice(TE9_DECLARATIONS)]
    else:
        selected_declarations = (
            [] if rng.random() < 0.1 else [rng.choice(PE3_DECLARATIONS)]
        )

    declaration = selected_declarations[0] if selected_declarations else None

    payload: dict[str, Any] = {
        "applicationDateReceived": iso(date_received),
        "applicationType": timeliness,
        "applicationForm": form,
        "applicationPenaltyChargeNumber": case_data.get("penaltyChargeNumber"),
        "applicationVehicleRegistration": case_data.get("vehicleRegistrationNumber"),
        "applicationApplicant": name,
        "applicationLocationOfContravention": rng.choice(LOCATIONS),
        "applicationDateOfContravention": iso(date_of_contravention),
        "applicationTitle": title,
        "applicationFullName": name,
        "applicationCompanyName": company or None,
        "applicationAddress": ", ".join(address_parts) if address_parts else "1 EXAMPLE STREET, LONDON",
        "applicationPostcode": postcode,
        "applicationDeclaration": declaration,
        # PDF-only: may contain 0–2 TE9 tickboxes; stripped before CCD submit.
        "_pdfDeclarations": selected_declarations,
    }

    if timeliness == "outOfTime":
        payload["applicationTe7Submitted"] = rng.choice(("Yes", "No"))

    if form == "PE3":
        payload["applicationReasonsGiven"] = rng.choice(("Yes", "No"))

    if form == "TE9" and "paidInFull" in selected_declarations:
        payload["applicationDatePaid"] = iso(date_of_contravention + timedelta(days=rng.randint(1, 20)))
        payload["applicationHowPaid"] = rng.choice(HOW_PAID)
        payload["applicationPaidTo"] = rng.choice(PAID_TO)

    # Drop nulls so CCD optional fields stay omitted
    return {k: v for k, v in payload.items() if v is not None and v != ""}


def unescape_pdf_name(value: str) -> str:
    return (
        value.replace("\\(", "(")
        .replace("\\)", ")")
        .replace("\\\\", "\\")
    )


def parse_te9_widgets(template: Path) -> dict[str, tuple[float, float, float, float]]:
    """TE9 widgets exist as orphaned objects (no AcroForm); parse /T and /Rect from bytes."""
    raw = template.read_bytes()
    widgets: dict[str, tuple[float, float, float, float]] = {}
    for match in re.finditer(rb"/Subtype/Widget/T\(((?:\\.|[^\\)])*)\)", raw):
        name = unescape_pdf_name(match.group(1).decode("latin1"))
        window = raw[max(0, match.start() - 250) : match.start()]
        rect = re.search(
            rb"/Rect\[\s*([0-9.]+)\s+([0-9.]+)\s+([0-9.]+)\s+([0-9.]+)\s*\]",
            window,
        )
        if rect:
            widgets[name] = tuple(float(g) for g in rect.groups())  # type: ignore[assignment]
    return widgets


def merge_overlay(template: Path, out_pdf: Path, draw) -> None:
    reader = PdfReader(str(template))
    page = reader.pages[0]
    width = float(page.mediabox.width)
    height = float(page.mediabox.height)

    packet = BytesIO()
    c = canvas.Canvas(packet, pagesize=(width, height))
    draw(c, width, height)
    c.save()
    packet.seek(0)
    overlay = PdfReader(packet)

    writer = PdfWriter()
    page.merge_page(overlay.pages[0])
    writer.add_page(page)
    for extra in reader.pages[1:]:
        writer.add_page(extra)

    with out_pdf.open("wb") as handle:
        writer.write(handle)


def draw_text_in_rect(c: canvas.Canvas, rect: tuple[float, float, float, float], text: str, font_size: float = 9) -> None:
    x0, y0, x1, y1 = rect
    text = (text or "").strip()
    if not text:
        return
    max_width = max(x1 - x0 - 4, 10)
    size = font_size
    c.setFillColor(black)
    while size > 6 and c.stringWidth(text, "Helvetica", size) > max_width:
        size -= 0.5
    c.setFont("Helvetica", size)
    c.drawString(x0 + 2, y0 + 4, text[:120])


def draw_check(c: canvas.Canvas, rect: tuple[float, float, float, float]) -> None:
    x0, y0, x1, y1 = rect
    c.setFont("Helvetica-Bold", max(8, min(12, y1 - y0)))
    c.setFillColor(black)
    c.drawCentredString((x0 + x1) / 2, y0 + 1, "X")


def fill_te9(template: Path, out_pdf: Path, payload: dict[str, Any]) -> None:
    widgets = parse_te9_widgets(template)
    title = payload.get("applicationTitle", "Mr")
    declarations = payload.get("_pdfDeclarations")
    if not isinstance(declarations, list):
        single = payload.get("applicationDeclaration")
        declarations = [single] if single else []
    how_paid = (payload.get("applicationHowPaid") or "").lower()
    postcode = payload.get("applicationPostcode") or ""
    outward, inward = split_postcode(postcode)

    text_values = {
        "Penalty charge number": payload.get("applicationPenaltyChargeNumber") or "",
        "Vehicle Registration No": payload.get("applicationVehicleRegistration") or "",
        "Applicant": payload.get("applicationApplicant") or "",
        "Location of Contravention": payload.get("applicationLocationOfContravention") or "",
        "Date of Contravention": payload.get("applicationDateOfContravention") or "",
        "Full name (witness)": payload.get("applicationFullName") or "",
        "Address": payload.get("applicationAddress") or "",
        "Postcode 1": outward,
        "Postcode 2": inward,
        "Company name if vehicle owned and registered by a company": payload.get("applicationCompanyName")
        or "",
        "Print full name": payload.get("applicationFullName") or "",
        "Date statement of truth signed": payload.get("applicationDateReceived") or "",
        "I believe - overtype field": "I believe",
    }

    checks: list[str] = []
    title_field = {
        "Mr": "Title - Mr",
        "Mrs": "Title - Mrs",
        "Miss": "Title - Miss",
        "Ms": "Title - Ms",
        "Other": "Title - Other",
    }.get(title)
    if title_field:
        checks.append(title_field)

    declaration_fields = {
        "didNotReceivePcn": "did not receive the penalty charge notice - yes",
        "madeRepresentationsNoRejection": "representations but no rejection notice - yes",
        "appealedToAdjudicator": "no response to the appeal - yes",
        "paidInFull": "penalty charge has been paid in full - yes",
    }
    for code in declarations:
        field = declaration_fields.get(code)
        if field:
            checks.append(field)

    if "paidInFull" in declarations:
        text_values["date paid in full"] = payload.get("applicationDatePaid") or ""
        text_values["To whom was it paid"] = payload.get("applicationPaidTo") or ""
        if "cash" in how_paid:
            checks.append("Paid in cash")
        if "cheque" in how_paid:
            checks.append("Paid by cheque")
        if "debit" in how_paid:
            checks.append("Paid by debit card")
        if "credit" in how_paid:
            checks.append("Paid by credit card")

    checks.append("Signed by witness")

    def draw(c: canvas.Canvas, _width: float, _height: float) -> None:
        for name, value in text_values.items():
            rect = widgets.get(name)
            if rect:
                draw_text_in_rect(c, rect, value, font_size=10 if "Address" not in name else 8)
        for name in checks:
            rect = widgets.get(name)
            if rect:
                draw_check(c, rect)

    merge_overlay(template, out_pdf, draw)


def stamp_pe3(template: Path, out_pdf: Path, payload: dict[str, Any]) -> None:
    """Overlay key values on the flat PE3 form (approximate A4 positions)."""

    def draw(c: canvas.Canvas, _width: float, height: float) -> None:
        c.setFont("Helvetica", 9)
        c.setFillColor(black)
        fields = [
            (220, height - 145, payload.get("applicationPenaltyChargeNumber") or ""),
            (220, height - 165, payload.get("applicationVehicleRegistration") or ""),
            (220, height - 185, payload.get("applicationApplicant") or ""),
            (220, height - 205, payload.get("applicationLocationOfContravention") or ""),
            (220, height - 225, payload.get("applicationDateOfContravention") or ""),
            (80, height - 320, payload.get("applicationFullName") or ""),
            (80, height - 340, payload.get("applicationAddress") or ""),
            (80, height - 360, payload.get("applicationPostcode") or ""),
            (80, height - 400, f"Date received: {payload.get('applicationDateReceived') or ''}"),
            (80, height - 415, f"Declaration: {payload.get('applicationDeclaration') or 'blank'}"),
            (80, height - 430, f"Reasons given: {payload.get('applicationReasonsGiven') or ''}"),
        ]
        for x, y, text in fields:
            if text:
                c.drawString(x, y, str(text)[:90])

    merge_overlay(template, out_pdf, draw)


def main() -> None:
    args = parse_args()
    rng = random.Random(args.seed)
    case_data = load_case(args.case_json)
    payload = build_application(case_data, args.type, args.form, rng)

    args.out_pdf.parent.mkdir(parents=True, exist_ok=True)
    args.out_payload.parent.mkdir(parents=True, exist_ok=True)

    if args.form == "TE9":
        fill_te9(args.template, args.out_pdf, payload)
    else:
        stamp_pe3(args.template, args.out_pdf, payload)

    ccd_payload = {k: v for k, v in payload.items() if not str(k).startswith("_")}
    args.out_payload.write_text(json.dumps(ccd_payload, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"payloadPath": str(args.out_payload), "pdfPath": str(args.out_pdf)}))


if __name__ == "__main__":
    main()
