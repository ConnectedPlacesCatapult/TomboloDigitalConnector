This directory contains a number of built-in model recipes that Tombolo
Digital Connector users can use. The recipes can be exported as self
contained fields or combined together using the Digital Connector field
constructs. Examples of uses of these models can be found above in
[examples](executions/methodologicalexamples) and
[city-indices](executions/methodologicalexamples).

At the time of writing, the built-in models are organised into several
sub-folders.
- census: contains recipes using the UK Census data.
- city-indices: is a collection of city indices, measuring varied
 phenomena, such as, risk of social isolation among the elderly and
 use and opportunities for active transport.
- environment: recipes from various sources for environmental data such
 as air quality and green-space.
- geometry: recipes for calculating qualities of geometries, such as
 area.
- transport: recipes for transport data, such as traffic counts.

Most of the recipes, except the city-indices, are meant to serve as
building-blocks for combined and integrated models.

The hierarchy of the sub-folders is purely for organisation purposes and
has no impact on the usage of the recipes:
Any recipe can be combined with any other recipe, independent of their
location in the storage hierarchy.

As with any hierarchy it is not always easy to locate a recipe in a
specific folder since it might belong to several.
For example, the recipe for getting the fraction of people using active
transport according to the UK Census could conceptually belong either to
the census folder or the transport folder.
Hence it is important that in the future recipes should be both
searchable and browsable using a tagging system (see issue #311).
