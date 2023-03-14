import React, { FunctionComponent, useEffect, useState } from 'react';
import { Alert, Button, Card, Layout, Spin } from "antd";
import 'antd/dist/antd.css';
import Sider from "antd/es/layout/Sider";
import { Content } from "antd/es/layout/layout";
import { fetchCandidates, fetchCountback } from "./api";
import { Container, ResultLine } from "./styles";
import { DeviceSelect } from "./DeviceSelect";

const CountbackApp: FunctionComponent = () => {

  const [menuCollapsed, setMenuCollapsed] = useState(false);
  const [candidatesMap, setCandidatesMap] = useState(null as Record<string, string[]> | null);
  const [fetchingCandidatesMap, setFetchingCandidatesMap] = useState(0);
  const [year, setYear] = useState(null as number | null);
  const [electorate, setElectorate] = useState(null as string | null);
  const [candidateToRetire, setCandidateToRetire] = useState(null as string | null);
  const [candidatesToContest, setCandidatesToContest] = useState([] as string[]);
  const [fetchingCountback, setFetchingCountback] = useState(false as boolean);
  const [countbackResult, setCountbackResult] = useState(null as JSX.Element[] | null);
  const [fetchError, setFetchError] = useState(null as string | null);

  useEffect(() => {
    if (year) {
      setFetchingCandidatesMap(c => c + 1);
      fetchCandidates(year).then(
        response => {
          setCandidatesMap(response);
          setFetchingCandidatesMap(c => c - 1);
        },
        reason => {
          setFetchError(reason.toString());
          setFetchingCandidatesMap(c => c - 1);
        }
      )
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
          trigger={fetchingCountback ? <Spin/> : undefined}
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
              <Card style={{ margin: 10 }}>
                <DeviceSelect
                  placeholder={"Select Election Year"}
                  style={{ width: "100%" }}
                  value={year}
                  onChange={(e) => {
                    setYear(e);
                    setElectorate(null);
                    setCandidateToRetire(null);
                    setCandidatesToContest([]);
                  }}
                  showSearch={true}
                  options={[
                    { label: "2020", value: 2020 },
                    { label: "2016", value: 2016 },
                    { label: "2012", value: 2012 },
                    { label: "2008", value: 2008 }
                  ]}
                />
              </Card>
              <Card style={{ margin: 10 }}>
                <DeviceSelect
                  placeholder={"Select Electorate"}
                  style={{ width: "100%" }}
                  value={electorate}
                  onChange={(e) => {
                    setElectorate(e);
                    setCandidateToRetire(null);
                    setCandidatesToContest([]);
                  }}
                  showSearch={true}
                  loading={fetchingCandidatesMap > 0}
                  options={
                    candidatesMap ? Object.keys(candidatesMap).map(e => ({ label: e, value: e })) : []
                  }
                />
              </Card>
              <Card style={{ margin: 10 }}>
                <DeviceSelect
                  placeholder={"Select Candidate to Retire"}
                  style={{ width: "100%" }}
                  value={candidateToRetire}
                  onChange={(c) => {
                    setCandidateToRetire(c);
                    setCandidatesToContest(cs => cs.filter(c1 => c1 !== c))
                  }}
                  showSearch={true}
                  loading={fetchingCandidatesMap > 0}
                  options={
                    candidatesMap && electorate && candidatesMap[electorate]
                      ? candidatesMap[electorate].map(c => ({ label: c, value: c }))
                      : []
                  }
                />
              </Card>
              <Card style={{ margin: 10 }}>
                <DeviceSelect
                  placeholder={"Select Candidates to Contest the Casual Vacancy"}
                  mode={"multiple"}
                  style={{ width: "100%" }}
                  value={candidatesToContest}
                  onChange={(c: string[]) => {
                    setCandidatesToContest(c.filter(n => n !== "*all*" && n !== "*none*"))
                  }}
                  onSelect={(s: string) => {
                    if (candidatesMap && electorate && candidatesMap[electorate]) {
                      if (s === "*all*") {
                        setCandidatesToContest(
                          candidatesMap[electorate].filter(c => c !== candidateToRetire)
                        )
                      } else if (s === "*none*") {
                        setCandidatesToContest([])
                      }
                    }
                  }}
                  showSearch={true}
                  loading={fetchingCandidatesMap > 0}
                  options={[
                    { label: "-- Select All --", value: "*all*" },
                    { label: "-- Deselect All --", value: "*none*" },
                    ...candidatesMap && electorate && candidatesMap[electorate]
                      ? candidatesMap[electorate].filter(c => c !== candidateToRetire).map(c => ({
                        label: c,
                        value: c
                      }))
                      : []
                  ]}
                />
              </Card>
              <div style={{ paddingBottom: 70 }}>
                <Button
                  style={{ margin: "auto", display: "block" }}
                  onClick={() => {
                    setCountbackResult([]);
                    setFetchingCountback(true);
                    try {
                      fetchCountback(
                        year ?? 2020,
                        electorate ?? "",
                        candidateToRetire ?? "",
                        candidatesToContest,
                        result => {
                          if (result === null) {
                            setFetchingCountback(false);
                          } else {
                            setCountbackResult(
                              initial => (initial ?? []).concat([
                                <ResultLine
                                  key={(initial?.length ?? 0) + 1}
                                  newParagraph={result.newParagraph ?? false}
                                >
                                  {result.message}
                                </ResultLine>
                              ])
                            );
                          }
                        }
                      );
                    } catch (e: any) {
                      setFetchingCountback(false);
                      setFetchError(e.toString());
                    }
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
          { fetchError && (
            <Alert
              type={"error"}
              banner={true}
              message={fetchError}
              closable={true}
              onClose={() => setFetchError(null)}
            />
          ) }
          <Container>
            {countbackResult}
          </Container>
        </Content>
      </Layout>
    </>
  );
}

export default CountbackApp;
