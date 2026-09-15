require 'govuk_tech_docs'

GovukTechDocs.configure(self)

# Relative links work with GitHub Pages project sites
# (username.github.io/repo-name/) without hardcoding http_prefix.
set :relative_links, true
activate :relative_assets
