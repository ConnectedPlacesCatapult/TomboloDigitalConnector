In this directory there are several model recipes related to land-use
and, in particular, land-use diversity. In the current version we focus
on 3 land-use values from the Open Street Map: residential, commercial
and retail. We chose these 3 as a basis for initial exploration as we
considered them to be the core values to consider in urban context.
A debate whether to include more values is welcome.

For each land-use value X we include two models:
- `fraction-X`: returns the area of all Open Street Map entities tagged
 with value X as a fraction of the output geography. E.g. if the output
 geography is an MSOA, the field will add up the area of all relevant
 entities and divide with the area of the MSOA.
- `fraction-X-of-urban`: returns the area of all Open Street Map entities
 tagged with value X as a fraction of the area of all Open Street Map
 entities tagged with one of the 3 chosen urban land-uses (residential,
 commercial and retail). Thee fields make use of the field `urban-area`
 which returns the cumulative area of the 3 urban land-uses.

For land-use diversity we include 4 models:
- `land-use-variance`: returns the variance among the 3 `fraction-X`
 sub-fields.
- `land-use-urban-variance`: returns the variance among the 3
 `fraction-X-of-urban` sub-fields.
- `land-use-coeffient-of-variance`: returns the coefficient of variance
 (stdev/mean) for the 3 `fraction-X` sub-fields.

These measures of land-use diversity are motivated by the intuition
that lower variance among the different land-use fractions make a
neighbourhood more diverse. This is un-proven though and up for debate.
