# Lets-Plot

[![official JetBrains project](http://jb.gg/badges/official-flat-square.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![License MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://raw.githubusercontent.com/JetBrains/lets-plot-kotlin/master/LICENSE)
[![Latest Release](https://img.shields.io/github/v/release/JetBrains/lets-plot)](https://github.com/JetBrains/lets-plot/releases/latest)


**Lets-Plot** is a multiplatform plotting library built on the principles of the Grammar of Graphics. 

The library' design is heavily influenced by Leland Wilkinson work [The Grammar of Graphics](https://www.goodreads.com/book/show/2549408.The_Grammar_of_Graphics) describing the deep features that underlie all statistical graphics.

> This grammar [...] is made up of a set of independent components that can be composed in many different ways. This makes [it] very powerful because you are not limited to a set of pre-specified graphics, but you can create new graphics that are precisely tailored for your problem.
> - Hadley Wickham, "[ggplot2: Elegant Graphics for Data Analysis](https://ggplot2-book.org/index.html)"


## Grammar of Graphics for Python [![Latest Release](https://badge.fury.io/py/lets-plot.svg)](https://pypi.org/project/lets-plot)

A bridge between R (ggplot2) and Python data visualization. \
To learn more see the documentation site at **[lets-plot.org](https://lets-plot.org)**.          


## Grammar of Graphics for Kotlin [![Latest Release](https://img.shields.io/github/v/release/JetBrains/lets-plot-kotlin)](https://github.com/JetBrains/lets-plot-kotlin/releases/latest)

### Notebooks
Create plots in [Kotlin Notebook](https://plugins.jetbrains.com/plugin/16340-kotlin-notebook),
[Datalore](https://datalore.jetbrains.com/report/static/HZqq77cegYd.E7get_WnChZ/aTA9lQnPkRwdCzT6uy95GZ), [Jupyter with Kotlin Kernel](https://github.com/Kotlin/kotlin-jupyter#readme) \
or any other notebook that supports `Kotlin Kernel`. \
To learn more see the **[Lets-Plot Kotlin API](https://github.com/JetBrains/lets-plot-kotlin)** project at GitHub.

### Compose Multiplatform
Embed Lets-Plot charts in [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) applications. \
To learn more see the **[Lets-Plot Skia Frontend](https://github.com/JetBrains/lets-plot-skia)** project at GitHub.

### JVM and Kotlin/JS
Embed Lets-Plot charts in JVM (Swing, JavaFX) and Kotlin/JS applications. <br>
To learn more see the **[Lets-Plot Kotlin API](https://github.com/JetBrains/lets-plot-kotlin)** project at GitHub.

## "Lets-Plot in SciView" plugin

[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/14379-lets-plot-in-sciview.svg)](http://plugins.jetbrains.com/plugin/14379-lets-plot-in-sciview)
[![JetBrains plugins](https://img.shields.io/jetbrains/plugin/d/14379-lets-plot-in-sciview.svg)](http://plugins.jetbrains.com/plugin/14379-lets-plot-in-sciview)

*Scientific mode* in PyCharm and in IntelliJ IDEA provides support for interactive scientific computing and data visualization.

[*Lets-Plot in SciView*](https://plugins.jetbrains.com/plugin/14379-lets-plot-in-sciview) plugin adds 
support for interactive plotting to IntelliJ-based IDEs with the *Scientific mode* enabled.
 
>
> **Note:** The *Scientific mode* is NOT available in communinty editions of JetBrains IDEs. 
>

Also read:

- [Scientific mode in PyCharm](https://www.jetbrains.com/help/pycharm/matplotlib-support.html)
- [Scientific mode in IntelliJ IDEA](https://www.jetbrains.com/help/idea/matplotlib-support.html)

## What is new in 4.4.0

- #### Waterfall Plot
  <img src="https://raw.githubusercontent.com/JetBrains/lets-plot/master/docs/f-24e/images/waterfall.png" alt="f-24e/images/waterfall.png" width="460" height="220">
  
  See [example notebook](https://nbviewer.org/github/JetBrains/lets-plot/blob/master/docs/f-24e/waterfall_plot.ipynb).

- #### **`geom_band()`**:
  <img src="https://raw.githubusercontent.com/JetBrains/lets-plot/master/docs/f-24e/images/geom_band.png" alt="f-24e/images/geom_band.png.png" width="615" height="220">

  See [example notebook](https://nbviewer.org/github/JetBrains/lets-plot/blob/master/docs/f-24e/geom_band.ipynb).

- #### Custom Legends 
  - `manual_key` parameter in plot layer
  - `layer_key()` function
  <br>
  <img src="https://raw.githubusercontent.com/JetBrains/lets-plot/master/docs/f-24e/images/custom_legend.png" alt="f-24e/images/custom_legend.png.png" width="294" height="147">

  See [example notebook](https://nbviewer.org/github/JetBrains/lets-plot/blob/master/docs/f-24e/manual_legend.ipynb).
   
- #### Customizing Legends Appearence
  The `override_aes` parameter in the `guide_legend()` function.
     
  See [example notebook](https://nbviewer.org/github/JetBrains/lets-plot/blob/master/docs/f-24e/legend_override_aes.ipynb).


- #### And More

  See [CHANGELOG.md](https://github.com/JetBrains/lets-plot/blob/master/CHANGELOG.md) for a full list of changes.


## Recent Updates in the [Gallery](https://lets-plot.org/python/pages/gallery.html)

  <a href="https://nbviewer.org/github/JetBrains/lets-plot-docs/blob/master/source/examples/demo/venn_diagram.ipynb">
    <img src="https://raw.githubusercontent.com/JetBrains/lets-plot/master/docs/f-24b/images/gal_venn_diagram.png" alt="f-24b/images/gal_venn_diagram.png" width="128" height="128">
  </a>
  <a href="https://nbviewer.org/github/JetBrains/lets-plot-docs/blob/master/source/examples/cookbook/geom_spoke.ipynb">
    <img src="https://raw.githubusercontent.com/JetBrains/lets-plot/master/docs/f-24b/images/gal_spoke.png" alt="f-24b/images/gal_spoke.png" width="128" height="128">
  </a>
  <a href="https://www.kaggle.com/code/alshan/indonesia-volcanoes-on-map">
    <img src="https://raw.githubusercontent.com/JetBrains/lets-plot/master/docs/f-24b/images/gal_indonesia_volcanoes_on_map.png" alt="f-24b/images/gal_indonesia_volcanoes_on_map.png" width="128" height="128">
  </a>
  <a href="https://www.kaggle.com/code/alshan/japanese-volcanoes-on-map">
    <img src="https://raw.githubusercontent.com/JetBrains/lets-plot/master/docs/f-24b/images/gal_japanese_volcanoes_on_map.png" alt="f-24b/images/gal_japanese_volcanoes_on_map.png" width="128" height="128">
  </a>
  <a href="https://nextjournal.com/asmirnov-horis/bbc-visual-and-data-journalism-cookbook-for-lets-plot">
    <img src="https://raw.githubusercontent.com/JetBrains/lets-plot/master/docs/f-24a/images/gal_bbc_cookbook.png" alt="f-24a/images/gal_bbc_cookbook.png" width="128" height="128">
  </a>
  <a href="https://nbviewer.org/github/JetBrains/lets-plot-docs/blob/master/source/examples/demo/palmer_penguins.ipynb">
    <img src="https://raw.githubusercontent.com/JetBrains/lets-plot/master/docs/f-24a/images/gal_penguins.png" alt="f-24a/images/gal_penguins.png" width="128" height="128">
  </a>
  <a href="https://nbviewer.org/github/JetBrains/lets-plot-docs/blob/master/source/examples/demo/periodic_table.ipynb">
    <img src="https://raw.githubusercontent.com/JetBrains/lets-plot/master/docs/f-24a/images/gal_periodic_table.png" alt="f-24a/images/gal_periodic_table.png" width="128" height="128">
  </a>
  <a href="https://nbviewer.org/github/JetBrains/lets-plot-docs/blob/master/source/examples/demo/wind_rose.ipynb">
    <img src="https://raw.githubusercontent.com/JetBrains/lets-plot/master/docs/f-24a/images/gal_wind_rose.png" alt="f-24a/images/gal_wind_rose.png" width="128" height="128">
  </a>
  <a href="https://nbviewer.org/github/JetBrains/lets-plot-docs/blob/master/source/examples/demo/heatmap_in_polar_coord.ipynb">
    <img src="https://raw.githubusercontent.com/JetBrains/lets-plot/master/docs/f-24a/images/gal_polar_heatmap.png" alt="f-24a/images/gal_polar_heatmap.png" width="128" height="128">
  </a>

## Change Log

[CHANGELOG.md](https://github.com/JetBrains/lets-plot/blob/master/CHANGELOG.md)


## Code of Conduct

This project and the corresponding community are governed by the
[JetBrains Open Source and Community Code of Conduct](https://confluence.jetbrains.com/display/ALL/JetBrains+Open+Source+and+Community+Code+of+Conduct).
Please make sure you read it.


## License

Code and documentation released under the [MIT license](https://github.com/JetBrains/lets-plot/blob/master/LICENSE).
Copyright © 2019-2024, JetBrains s.r.o.
