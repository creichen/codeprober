# Workspace Settings

The `.settings` file in the workspace root may contain JSON data such as:

```
{ "defaultProbes": [ "*:reports" ] }
```

The options are listed below:

- `defaultProbes`: A list of strings of the format `<type>:<attr>` or `<type>:<attr>:-`
  that will add pervasive, minimised default probes to the workspace.
  `<type>` is an AST node type, `<attr>` is the name of an attribute.  The optional `:-` suffix indicates
  that the probe should hide diagnostics by default.

