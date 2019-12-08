# Steins;Gate worldlines poster

This code was used to generate a poster based on the visual novel Steins;Gate.

The poster was first published on Reddit here:
https://www.reddit.com/r/steinsgate/comments/81t7by/i_made_a_poster_plotting_the_timetravel_and/

A Java program was used to generate the base layout of the poster, by outputting a VBA macro as a series of function calls to run in PowerPoint. Human formatting converted the automated layout into the final poster.

## Provided files

- notes/
  - notes.xlsx

The original notes that form the basis of the poster. For each worldline, events are listed in chronological order (from Rintaro's perspective) with date and time included (where known). Not included are any inferences about if these events would have occurred on other worldlines.

- code/input/
  - endFreeEvents.in
  - kaikanFreeEvents.in
  - lineNames.in
  - part1Events.in
  - part1LineOrder.in
  - timeLeaps.in

The input files used by the formatting code to generate the base layout for the poster. For detailed descriptions of the formatting of each file, see 'File Descriptions.txt'.

- code/src/
  - CrossingLineSorter.java
  - PosterCodeFormatter.java

The Java code used to determine the base layout in the form of a VBA macro. The code is provided without documentation.

- code/output/
  - runMacro.out

The output of the formatting code. To be run as a macro with the functions in 'poster/Macro Code.txt'.

- poster/
  - Final Arranged Poster.pptx
  - Final Poster.pdf
  - Final Poster.png
  - Macro Code.txt
  - Unarranged Poster.pptm

The poster itself. 'Macro Code.txt' is a copy of the functions used to generate the base layout. 'Unarranged Poster.pptm' (PowerPoint Macro-Enabled file) is the complete macro, and the automated base layout of the poster before any human formatting occurred. 'Final Arranged Poster.pptx' (PowerPoint file) is the finished poster, after all human formatting. This has been converted to PDF ('Final Poster.pdf') and PNG ('Final Poster.png') forms. The poster is A0 landscape in size. To display the PowerPoint files correctly, the 'Droid Sans Fallback' font must be installed (not provided here, but available under the [Apache License](https://www.apache.org/licenses/)).
