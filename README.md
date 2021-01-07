# eps2pgf
copied from https://sourceforge.net/projects/eps2pgf/
# Install 
```bat
ant jar
```
---
author:
- Paul Wagenaars
date: '@BUILDDATE@'
title: 'Eps2pgf @VERSION@ User Manual'
---

Introduction
============

Eps2pgf is a PostScript interpreter that converts [Encapsulated
PostScript](http://en.wikipedia.org/wiki/Encapsulated_PostScript) (EPS)
figures to the [Portable Graphics
Format](http://sourceforge.net/projects/pgf/) (PGF). PGF/TikZ is a TeX
macro package for generating graphics. It support several back-end
drivers, including pdfTeX and Dvips. The major advantage of Eps2pgf is
that all texts are typeset by LaTeX, giving you all the powerful
typesetting features and a uniform look of the final document. It has
several options to control how text in figures is handled: (i) reproduce
text labels accurately (with same font size and formatting as in EPS
figure), (ii) copy text labels verbatim (text in EPS figure is LaTeX
code), or (iii) replace text labels using
[](http://www.ctan.org/tex-archive/help/Catalogue/entries/psfrag.html)-compatible
rules from a separate file, or using tags embedded in the text labels.

The goal of Eps2pgf is to support all PostScript figures created by
programs regularly used by LaTeX users to create figures, such as
MATLAB, Mathematica and Maple. If you encounter a figure that Eps2pgf
fails to process, please report it using the bug tracker
(<http://sourceforge.net/tracker/?group_id=188852&atid=926973>), or send
it via email.

Requirements
============

-   Java Runtime Environment (version 1.5 or higher)

-   LaTeX, with the package

Command line arguments
======================

`java -jar eps2pgf.jar <input file> -o <output file>`

`<input file>`

`(-o|–output) <output file>`

The following arguments are optional:

`[(-m|–text-mode) <text mode>]`

`[–text-replace <text replace file>]`

`[–verbose]`

`[–version]`

`[-h|–help]`

Including PGF figures in LaTeX documents
========================================

After the the PGF figure has been created it can be included in LaTeX
documents. The package is required in order to use PGF figures. A
minimal example can be found in figure \[fig:minimal\_example\].

    \documentclass{article}

    \usepackage{pgf}

    \begin{document}
        \begin{figure}
            \centering
            \input{figure.pgf}
            \caption{pgf figure}
        \end{figure}
    \end{document}
            

Text handling
=============

Eps2pgf can handle text labels in PostScript figures in various ways. By
default it will try to reproduce the text labels as accurately as
possible, while using the default font in the LaTeX document. That means
that it will use the same font size, style and formatting as in the EPS
figure. The center of the text label in the output is aligned with the
center of the text label in the PostScript figure.

In the second mode, invoked using the command line argument
`–text-mode directcopy`, the text in the text labels is directly copied
to the PGF figure. This allows you to use custom LaTeX code in the
figure. Unless specified otherwise the center of the text label in the
output is aligned with the center of the text label in the PostScript
figure. Additionally, it is possible to specify anchor, scaling and
rotation using the -style tag as text label in the PostScript figure:

    \tex[pgfanchor][psanchor][scale][rotation]{LaTeX text}
        

The first four arguments are optional, the last argument is required.

-   $[$pgfanchor$]$ — the LaTeX text reference point. It specifies both
    the vertical and the horizontal alignment. One of the letters `t`,
    `c`, `B` or `b` (top, center, baseline, bottom) specifies the
    vertical alignment, and one of the letters `l`, `c` or `r` (left,
    center, right) specifies the horizontal alignment. For example,
    `[br]` indicates that the anchor is the bottom-right corner of the
    text label. If the vertical or horizontal alignment is omitted, then
    `c` is used. If the argument is omitted completely, `[Bl]` is used.

-   $[$psanchor$]$ — the PostScript text reference point. This argument
    has the same formatting as the pfganchor argument.

-   $[$scale$]$ — Scaling factor for font size. It is recommended not to
    use this parameter, it’s better to specify the font size using
    LaTeX’s font sizing commands. Default: `[1]`.

-   $[$rotation$]$ — Extra rotation of the text. The rotation specified
    here is added to the rotation of the text in the PostScript figure.
    Default: `[0]`.

-   $\{$LaTeX text$\}$ — LaTeX code for the text label.

It is also possible to use text replacement rules, which are specified
in a separate file. An external file with replacement rules can be
specified using the command line argument
`–text-replace <text replace file>`. The rules in this text replacement
file specify which text labels must be replace by another text. The file
can contain one or more of these rules. These rules follow the exact
same syntax as the package:

    \psfrag{text}[pgfanchor][psanchor][scale][rotation]{LaTeX text}
    \psfrag*{text}[pgfanchor][psanchor][scale][rotation]{LaTeX text}
        

The first and last arguments are required, the other four arguments are
optional.

-   $\{$text$\}$ — text in the PostScript figure that will be replaced
    by the LaTeX text in the last argument.

-   $[$pgfanchor$]$ — the LaTeX text reference point. It specifies both
    the vertical and the horizontal alignment. One of the letters `t`,
    `c`, `B` or `b` (top, center, baseline, bottom) specifies the
    vertical alignment, and one of the letters `l`, `c` or `r` (left,
    center, right) specifies the horizontal alignment. For example,
    `[br]` indicates that the anchor is the bottom-right corner of the
    text label. If the vertical or horizontal alignment is omitted, then
    `c` is used. If the argument is omitted completely, `[Bl]` is used.

-   $[$psanchor$]$ — the PostScript text reference point. This argument
    has the same formatting as the pfganchor argument.

-   $[$scale$]$ — Scaling factor for font size. It is recommended not to
    use this parameter, it’s better to specify the font size using
    LaTeX’s font sizing commands. Default: `[1]`.

-   $[$rotation$]$ — Extra rotation of the text. The rotation specified
    here is added to the rotation of the text in the PostScript figure.
    Default: `[0]`.

-   $\{$LaTeX text$\}$ — LaTeX code for the text label.

Note: Eps2pgf does not correctly handle the starred `\psfrag*` command.
Eps2pgf treats the starred version exactly the same as the normal
`\psfrag` command, while handles it slightly different.

As a demonstration of the different text modes a figure is converted
using different text modes. The original figure, before conversion by
Eps2pgf, can be found in figure \[fig:demo\_figure\_orig\]. Converting
this figure with Eps2pgf with default options results in
figure \[fig:demo\_figure\_std\]. As you can see it looks pretty similar
to the original. It uses the sans-serif font, the label is bold, and the
font size is the same. The only difference is the font itself. Next, the
same figure is converted with text mode `directcopy` and an external
file with the following text replacement rules:

    \psfrag{xlabel}[cc][cc]{Replaced \texttt{xlabel}}
    \psfrag{eq}[bc][tl]{$y = \sin(2x) + \sqrt{x}$}
        

The resulting figure is figure \[fig:demo\_figure\_repl\]. All labels
use the standard text font and formatting. The title label is replaced
using the inline `\tex[][]{}` rule. The and labels are replaced using
the rules in the external file. Note the usage of the pgf- and psanchor
in the rule for the label.

Copyright and license
=====================

See the files `NOTICE.txt` and `LICENSE.txt`. Or run Eps2pgf with the
command line option `–version`.
