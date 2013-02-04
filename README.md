mp3andburn
==========

A Java application written for a very specific purpose of burning a CD and converting and tagging an MP3 for distribution after a church service in one particular church.  The code does not attempt to generalise to other uses but may provide a useful basis for forking an alternative version.

Prerequisites
-------------

* The software is designed to be run on a Macintosh (it makes assumptions, for example, as to where log files should be written that are Mac specific.)
* The software assumes Java 7 for it's built in support for JavaFX 2.  The software is written in Scala.
* The software relies on a number of command line tools having been already installed on the machine.  These carry out the actual audio processing and CD recording work.  This software simply provides a convenient interface for entering ID tags, controlling and displaying feedback on the ongoing process.
* The command line tools are: [cdrecord](http://cdrecord.berlios.de/private/cdrecord.html), [normalize](http://normalize.nongnu.org/), [lame](http://lame.sourceforge.net/) and [sox](http://sox.sourceforge.net/).  These were installed using Homebrew and the command:

        brew install cdrtools normalize lame sox

---

This software is Copyright (c) 2011, Stuart Roebuck and licensed under the very open MIT license (more specifically referred to as the Expat license). See the `LICENSE.md` file for details.

