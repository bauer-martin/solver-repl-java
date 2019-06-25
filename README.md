# REPL for Java Solvers

The REPL can be used directly from the command line or from [SPL Conqueror](https://github.com/bauer-martin/SPLConqueror).

## Commands

-   An option is represented according to the selected option coding.
-   A configuration is represented as a list of options separated by comma:  `Option1,Option2,Option3`
-   A list of configurations is separated by semicolon: `Option1,Option2;Option2,Option3;Option1,Option3`

### Loading a Variability Model

```text
load-vm <path/to/vm/>
```

Every solver command relies on a global variability model.
Use this command to load the model from an XML file.

### Configuring a Solver

```text
select-solver <solver>
```

Use this command to select a solver which is used in all subsequent solver commands.
Currently, the following solvers are available:

| Name  | Solver  | Description                                         | Link                          |
| ----- | ------- | --------------------------------------------------- | ----------------------------- |
| Choco | `choco` | Open-Source Java Library for Constraint Programming | <http://www.choco-solver.org> |
| JaCoP | `jacop` | Java-based Open-Source Solver                       | <http://www.jacop.eu>         |

* * *

```text
set-solver-parameters <parameters>
```

Use this command to set a solver's parameters.
Currently, the following parameters are available.

| Name | Value            | Description                             | Supported By |
| ---- | ---------------- | --------------------------------------- | ------------ |
| seed | Positive Integer | Random Seed for Random Number Generator | Choco, JaCoP |

### Configuring the Output

```text
select-option-coding <coding>
```

Use this command to select the coding of the options in the variability model.
Currently, the following codings are available:

| Name                    | Description                                                          |
| ----------------------- | -------------------------------------------------------------------- |
| option-name             | Identifies options by their name.                                    |
| variability-model-index | Identifies options by their index in the sorted list of all options. |

### Checking Satisfiability

```text
check-sat <configuration> (complete|partial)
```

Checks whether the given selection is valid w.r.t. the variability model.

**Returns:** `true` or `false`

### Other Commands

```text
find-minimized-config <configuration> [<option1>,...]
```

Searches for all largest (in terms of selected binary options) valid configurations, based on the given (partial) configuration.
Optionally, additional options can be specified, which **should** not be part of the minimized configuration.

**Returns:** a configuration or `none`

* * *

```text
find-all-maximized-configs <configuration> [<option1>,...]
```

Searches for all the smallest (in terms of selected options) valid configurations, based on the given (partial) configuration.
Optionally, additional options can be specified, which **should** not be part of the maximized configurations.

**Returns:** a list of configurations or `none`

* * *

```text
generate-up-to <number>
```

Generates up to `<number>` configurations of the variability model.

**Return** a list of configurations or `none`

* * *

```text
generate-config-without-option <configuration> <option>
```

Searches for a configuration which is similar to the given configuration, but does not contain the given option.
If further options need to be removed from the given configuration, they are returned as well.

**Returns:** a configuration together with a (possibly empty) list of options or `none`

* * *

```text
generate-all-variants <option1>,...
```

Generates all valid combinations of the given options with respect to the variability model.

**Returns:** a list of partial configurations or `none`

* * *

```text
generate-config-from-bucket <numberOfSelectedOptions> [<option1>=<weight1>;...]
```

Searches for a configuration with the given number of selected options, optionally respecting the given option weighting.
Configurations that have been returned previously will not be returned again.

**Returns:** a configuration or `none`

* * *

```text
clear-bucket-cache
```

Clears the bucket cache used by the `generate-config-from-bucket` command.
This way, configurations that have been returned previously can be returned again.
