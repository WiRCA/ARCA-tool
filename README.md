# ARCA-tool Readme

ARCA-tool is the finalist project of the [Software Development Project (2011-2012)](http://www.soberit.hut.fi/T-76.4115/) course in Aalto University.

ARCA-tool is a tool for doing root cause analysis. It is implemented as a web application, with Play! framework.

Current version includes:

  - Registering user accounts
  - Login with Google Login
  - Making cause-effect diagrams for RCA Cases
    - Adding subcauses to causes
    - Adding additional relations between causes
    - Deleting causes
    - Renaming causes
    - Adding corrective actions for causes
    - Recommending and de-recommending causes
    - Creating classifications for causes
    - Tagging causes with classifications
  - Opening a view with the RCA Case condensed into a classification diagram
    - Opening relations between classifications to see a deeper dimension
    - Seeing the causes related to different classification relations
    - Being able to name the relations
  - Viewing percentage statistics and charts about the cause classification distribution
    - Categorized by percentage of total amount/ with corrective actions /with recommendations
    - Also pie charts related to the abovementioned figures
  - Sharing RCA Cases by email
  - Users can be invited even if they do not have an account
  - Cause-effect diagrams can be made simultaneously by multiple people, collaboratively
  - Causes can be downloaded as a cvs file
  - Causes and corrective actions can be monitored through a separate monitoring view
    - Search can be filtered with multiple ways
    - Causes and corrective actions can be liked
    - States of causes and corrective actions can be changed

# Installation

## Play! framework

Download Play! framework from [**HERE**](http://koti.kapsi.fi/risto/play-master-e0400da.zip) (package built from master branch, source code available at [e0400da60371c365063fccf941663d1e7c237938](https://github.com/playframework/play/commit/e0400da60371c365063fccf941663d1e7c237938)).

We used the above version of the Play! framework, because the latest stable version (1.2.3) [didn't have support for multiple databases](https://play.lighthouseapp.com/projects/57987/tickets/1129-play-documentation-and-milestones-out-of-synch) and a previous version from Github had a [bug](https://play.lighthouseapp.com/projects/57987/tickets/1037) with [ArchivedEventStream](http://www.playframework.org/documentation/api/1.2/play/libs/F.ArchivedEventStream.html).

Add the installation path to [environment variable](http://en.wikipedia.org/wiki/Environment_variable) `PATH`.

## Database

You can configure Play! framework to use [different database implementations](http://www.playframework.org/documentation/1.2.3/configuration#dbconf). Implemented configuration uses [MySQL](http://www.mysql.com/downloads/)-database.

Note that in-memory and filesystem databases do not work in Play!'s production environment.

## Java 1.6 JDK

Install [Java 1.6 JDK](http://jdk6.java.net/) and add the installation path to [environment variable](http://en.wikipedia.org/wiki/Environment_variable) `JAVA_HOME`.

## ARCA-tool source

Get the latest source from [Github](https://github.com/WiRCA/ARCA-tool/zipball/master) and unzip it somewhere.

## Installation steps

  1. Open command line utility and go to the folder where you unzipped the ARCA-tool source.
  2. Remove line `- play -> cobertura 2.4` from `conf/dependencies.yml` file. Released cobertura module does not work with latest play version.
  3. Get module dependencies by running `play deps --sync`.
  4. Edit conf/application.conf and comment/uncomment the database configuration (commented with `# Production environment database configuration`) you use. You can also configure other [Play! configuration parameters](http://www.playframework.org/documentation/1.2.3/configuration), such as mail server, port, etc.
  5. Migrate your MySQL-database:
    1. Create databases by running `play migrate:create --%prod`.
    2. Create database tables by running `play migrate:up --%prod`.
  6. Run the application by running `play run --%prod`. You can run the application in background by running `play start --%prod`.
  7. With default configuration, your ARCA-tool application is now available to use at `http://localhost:9900/prod`.
