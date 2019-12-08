# File Format Descriptions

- endFreeEvents.in
- kaikanFreeEvents.in

'endFreeEvents.in' describes the events after the dismantling of the Phonewave (August 21, 2010 - 2036).
'kaikanFreeEvents.in' describes the events surrounding Rintaro's and Kurisu's first meeting (July 28, 2010).
Both have the same format.

The first line is a single number `n`. `n` lines follow, each describing an event or block of simultaneous events.

Each event block is a series of integers, followed by one or more comments, each preceded by `#`. The first number `s` is the number of active lines at that block. `s` numbers follow, representing the indexes of worldlines in the order to be displayed. The next number `e` is the number of events in the block. `e` pairs of numbers `a_i b_i` follow. The ith pairing describes an event that covers `b_i` lines, starting at the `a_i`th line (0-indexed) in the ordering. The first comment is the `e` event descriptions separated by `/`s. If any comments include the case-sensitive text `shift`, the event represents a worldline shift between lines in event `0`, with an additional invisible event `1` that maintains other lines as active.

- lineNames.in

The list of worldline names.

- part1Events.in

Describes the events before the development of the Time Leap Machine (1921 - August 11, 2010).

The first line is a pair of numbers `w n`. The second line are the integers from `0` to `w-1` (inclusive), not in order. `n` lines follow, each describing an event or block of simultaneous events, in reverse chronological order. The ordering in the second line is the intended order of the lines at the first event in the list (last chronologically) when displayed on the poster.

Each event block is formatted as `w+1` integers, followed by one or more comments, each preceded by `#`. The first number `e` is the number of events in the block. The ith number is the event index of line (i-1), between `-1` and `e-1` (inclusive), with `-1` dictating that no event occurs for that line. The first comment is the `e` event descriptions separated by `/`s. If any comments include the case-sensitive text `shift`, the event represents a worldline shift between lines in event `0`, with an additional invisible event `1` that maintains other lines as active.

- part1LineOrder.in

Describes the order of the lines displaying the first block on the poster, as described in 'part1Events.in'.

Each line begins with an integer `e` followed by a block of `w` comma-separated integers from `0` to `w-1` (inclusive) enclosed in square brackets. Each line indicates that before the `e`th event (when ordered reverse chronologically), the ordering of the lines should change to the order in the block.

- timeLeaps.in

Describes the events involving the use of the Time Leap Machine (August 11 - August 21, 2010).

Events are described as minor or major, with major events appearing on the combined block displaying all worldlines simultaneously, and minor events only appearing on the more detailed blocks for each individual worldline. Some events are marked as both. Each worldline is represented as several sublines.

The first line is a single number `n`. `n` lines follow, each describing an event that may occur across multiple lines.

Each event begins with an integer `t` determining the chronological order (not from Rintaro's perspective, but actual time) of events. In most cases `t` is the date and time. Events with equal `t` occur simultaneously. `t` is followed by an identifier `i` describing the type of event. Some values follow, depending on `i`, followed by optional comments, each preceded by `#`. The first comment (if provided) is the description of the event.

Possible values of `i` are:

`begin` - followed by a single integer `l` - introduces a worldline `l`; the `t` value must precede all other references to `l`

`timeleap` - followed by two integers `l s` - introduces subline `s` of line `l`; represents the arrival point of the `s`th time leap of worldline `l`, creating a branching point overriding the previous events of `l`

`split` - followed by two integers `l s` - introduces a split of subline `s` of line `l` into two sublines `sa` and `sb`; represents an alternative ending

`shift` - followed by three integers `l0 s0 l1` - describes a connection from subline `s0` of line `l0` to subline `0` of `l1`; if `l1` is a new line, it is introduced directly below `s0`; represents a worldline shift

`minor`, `major`, `both` - followed by two integers `l s` - describes an event that occurs on a single subline `s` of line `l`

`multiminor`, `multimajor`, `multiboth` - followed by three integers `l c s` - describes an event that occurs across `c` sublines starting at subline `s` of line `l`; `c` includes only active lines (i.e. not sublines not yet introduced, nor terminated sublines), and assumes the ordering of worldlines as given by the `begin` lines

Any subline is terminated at a split, a shift, or if any of the comments contains the case-sensitive text `END` or `time leaps`.