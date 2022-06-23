# ACT Election Countback Estimator

This tool simulates the Hare Clark preference distribution and countback procedure used in the Australian Capital Territory for filling casual vacancies.

The tool is written in Kotlin and contains classes and methods for performing preference distributions and countbacks. It also has a web server with endpoints that allow for API calls to perform these counts. The counting logic was initially ported from https://github.com/liamblake/hcvote but has been heavily modified.

I've also included a simple web client bootstrapped using create-react-app that provides a UI for performing these counts.

**You can find a hosted version of the tool [here](http://countback.bouckaert.io/).**

## Building
To build the server, first retrieve ballot paper preference data from the [Elections ACT website](https://www.elections.act.gov.au/elections_and_voting/past_act_legislative_assembly_elections).
Put the relevant txt files in `src/main/resources/electiondata/`.

Optionally, [produce a build of the web client](https://github.com/ramonbouckaert/countback/blob/main/src/main/client/README.md) and place the built files in `src/main/resources/webroot`.

Then run `mvn clean package` in the root directory.
