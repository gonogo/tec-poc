# Technical Documentation

This project uses the [Tech Docs Template][template], which is a [Middleman template][mmt] that you can use to build technical documentation using a GOV.UK style.

## Preview your changes locally

```sh
bundle exec middleman server -p 4568
```

See the generated website on `http://localhost:4568`. Or from the repo root:

```sh
./bin/start-tech-docs.sh
```

If you make changes to the `config/tech-docs.yml` configuration file, you need to restart Middleman to see the changes.

## Build

```
bundle exec middleman build
```

## Licence

Unless stated otherwise, the codebase is released under the [MIT License][mit].
The documentation is [© Crown copyright][copyright] and available under the terms of the [Open Government 3.0][ogl] licence.

[mit]: LICENCE
[copyright]: http://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/uk-government-licensing-framework/crown-copyright/
[ogl]: http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
[mmt]: https://middlemanapp.com/advanced/project_templates/
[template]: https://github.com/alphagov/tech-docs-template
