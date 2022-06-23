import React, { FunctionComponent, useEffect, useState } from 'react';
import { Button, Card, Col, Layout, Row, Select, Spin } from "antd";
import 'antd/dist/antd.css';
import Sider from "antd/es/layout/Sider";
import { CandidatesMap } from "./types";
import { Content } from "antd/es/layout/layout";

const { Option } = Select;

const API_URL = ""

const App: FunctionComponent = () => {

  const [menuCollapsed, setMenuCollapsed] = useState(false);
  const [candidatesMap, setCandidatesMap] = useState(null as CandidatesMap | null);
  const [fetchingCandidatesMap, setFetchingCandidatesMap] = useState(0);
  const [year, setYear] = useState(null as number | null);
  const [electorate, setElectorate] = useState(null as string | null);
  const [candidateToRetire, setCandidateToRetire] = useState(null as string | null);
  const [candidatesToContest, setCandidatesToContest] = useState([] as string[]);
  const [iframeSrc, setiFrameSrc] = useState(undefined as string | undefined);

  useEffect(() => {
    if (year) {
      setFetchingCandidatesMap(c => c + 1);
      fetch(`${API_URL}api/candidates/${year}`).then(response => {
        response.json().then(json => {
          setCandidatesMap(json);
          setFetchingCandidatesMap(c => c - 1);
        })
      })
    }
  }, [year]);

  return (
    <>
      <Layout style={{ height: '100%' }}>
        <Sider
          collapsible={true}
          collapsed={menuCollapsed}
          onCollapse={collapsed => setMenuCollapsed(collapsed)}
          width={"370"}
          style={{ backgroundColor: "#7a9ab8", height: "100%", overflowX: "auto", maxWidth: "100%" }}
        >
          {!menuCollapsed && (
            <>
              <Card title={"ACT Election Countback Simulator"} style={{ margin: 10 }}>
                <p>
                  {"This tool provides a simulation of the election procedure for filling casual vacancies in the ACT Legislative Assembly "}
                  {"under their Hare-Clark electoral system. This is done by first estimating the original distribution of preference flows in that "}
                  {"electorate. Then, the ballots that contributed to the election of the retiring MLA are determined. Finally, a countback "}
                  {"can be run using an arbitrary set of candidates to nominate to contest the vacancy. For more information on how countbacks are performed, visit the "}
                  <a
                    href={"https://www.elections.act.gov.au/education/act_electoral_commission_fact_sheets/fact_sheets_-_general_html/elections_act_factsheet_casual_vacancies"}>Elections
                    ACT website</a>.
                </p>
                <p>
                  {"While this tool is fairly accurate, it doesn't reproduce the counts of the original distribution perfectly. "}
                  {"Counting Hare Clark is hard and there are small procedural differences that vary year by year. "}
                  {"The margin of error here is small (<0.5%) but it's still only an estimation. Don't rely on the results. "}
                  {"If you want perfect counts, you'll need to use something like "}
                  <a href={"https://github.com/AndrewConway/ConcreteSTV"}>ConcreteSTV</a>.
                </p>
                <p>
                  {"Source code for this tool is available "}
                  <a href={"https://github.com/ramonbouckaert/countback"}>here</a>.
                </p>
              </Card>
              <Card title={"Select Election Year"} style={{ margin: 10 }}>
                <Select
                  style={{ width: "100%" }}
                  value={year}
                  onChange={(e) => {
                    setYear(e);
                    setElectorate(null);
                    setCandidateToRetire(null);
                    setCandidatesToContest([]);
                  }}
                  showSearch={true}
                >
                  <Option key={2020}>2020</Option>
                  <Option key={2016}>2016</Option>
                  <Option key={2012}>2012</Option>
                  <Option key={2008}>2008</Option>
                </Select>
              </Card>
              <Card title={"Select Electorate"} style={{ margin: 10 }}>
                <Select
                  style={{ width: "100%" }}
                  value={electorate}
                  onChange={(e) => {
                    setElectorate(e);
                    setCandidateToRetire(null);
                    setCandidatesToContest([]);
                  }}
                  showSearch={true}
                  loading={fetchingCandidatesMap > 0}
                >
                  {candidatesMap && Object.keys(candidatesMap).map(e => <Option key={e}>{e}</Option>)}
                </Select>
              </Card>
              <Card title={"Select Candidate to Retire"} style={{ margin: 10 }}>
                <Select
                  style={{ width: "100%" }}
                  value={candidateToRetire}
                  onChange={(c) => {
                    setCandidateToRetire(c);
                    setCandidatesToContest(cs => cs.filter(c1 => c1 !== c))
                  }}
                  showSearch={true}
                  loading={fetchingCandidatesMap > 0}
                >
                  {candidatesMap && electorate && candidatesMap[electorate]
                    ? candidatesMap[electorate].map(c => <Option key={c.name}>{c.name}</Option>)
                    : []}
                </Select>
              </Card>
              <Card title={"Select Candidates to Contest the Casual Vacancy"} style={{ margin: 10 }}>
                <Select
                  mode={"multiple"}
                  style={{ width: "100%" }}
                  value={candidatesToContest}
                  onChange={(c) => {
                    setCandidatesToContest(c.filter(n => n !== "*all*" && n !== "*none*"))
                  }}
                  onSelect={(s: string) => {
                    if (candidatesMap && electorate && candidatesMap[electorate]) {
                      if (s == "*all*") {
                        setCandidatesToContest(
                          candidatesMap[electorate].filter(c => c.name !== candidateToRetire).map(c => c.name)
                        )
                      } else if (s == "*none*") {
                        setCandidatesToContest([])
                      }
                    }
                  }}
                  showSearch={true}
                  loading={fetchingCandidatesMap > 0}
                >
                  <Option key={"*all*"}>-- Select All --</Option>
                  <Option key={"*none*"}>-- Deselect All --</Option>
                  {candidatesMap && electorate && candidatesMap[electorate]
                    ? candidatesMap[electorate].filter(c => c.name !== candidateToRetire).map(c => <Option
                      key={c.name}>{c.name}</Option>)
                    : []}
                </Select>
              </Card>
              <div style={{ paddingBottom: 70 }} >
              <Button
                style={{ margin: "auto", display: "block" }}
                onClick={() => {
                  const params = new URLSearchParams({
                    "electorate": electorate ?? "",
                    "candidateToResign": candidateToRetire ?? "",
                    "candidatesToContest": candidatesToContest.join(";")
                  });
                  setiFrameSrc(encodeURI(`${API_URL}api/countback/${year}?${params.toString()}`));
                  setMenuCollapsed(true);
                }}
                loading={fetchingCandidatesMap > 0}
              >
                Run Countback
              </Button>
              </div>
            </>
          )
          }
        </Sider>
        <Content>
          <iframe src={iframeSrc} style={{ width: "100%", height: "100%", display: "block" }} frameBorder={0}/>
        </Content>
      </Layout>
    </>
  );
}

export default App;
