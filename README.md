# ACT Election Countback Simulator

This tool provides a simulation of the election procedure for filling casual vacancies in the ACT Legislative Assembly
under their Hare-Clark electoral system. This is done by first estimating the original distribution of preference flows
in that electorate. Then, the ballots that contributed to the election of the retiring MLA are determined. Finally, a
countback can be run using an arbitrary set of candidates to nominate to contest the vacancy. For more information on
how countbacks are performed, visit the relevant page on the
[Elections ACT website](https://www.elections.act.gov.au/education/act_electoral_commission_fact_sheets/fact_sheets_-_general_html/elections_act_factsheet_casual_vacancies).

While this tool is fairly accurate, it doesn't reproduce the counts of the original distribution perfectly. Counting
Hare Clark is hard and there are small procedural differences that vary year by year. The margin of error here is small
(<0.5%) but it's still only an estimation. Don't rely on the results. If you want perfect counts, you'll need to use
something more advanced like [AndrewConway/ConcreteSTV](https://github.com/AndrewConway/ConcreteSTV). However, I am open
to feedback and suggestions about how to improve my code to run more accurate preference distributions.

The tool is written in Kotlin and contains classes and methods for performing preference distributions and countbacks.
It also has a web server with endpoints that allow for API calls to perform these counts. The counting logic was
initially ported from [liamblake/hcvote](https://github.com/liamblake/hcvote) but has been heavily modified.

I've also included a simple web client bootstrapped using [facebook/create-react-app](https://github.com/facebook/create-react-app)
that provides a UI for performing these counts.

**You can find a hosted version of the tool [here](http://countback.bouckaert.io/).**

## Building
To build the server, first retrieve ballot paper preference data from the relevant page on the
[Elections ACT website](https://www.elections.act.gov.au/elections_and_voting/past_act_legislative_assembly_elections)
for 2020, 2016, 2012 and 2008. Put the relevant txt files in `src/main/resources/electiondata/{year}/`.

Then run `mvn clean package` in the root directory.
