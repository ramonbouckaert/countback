import { sendWwMessage, sendSwMessageMultipart } from "./index";

export const fetchCandidates = (year: number): Promise<Record<string, string[]>> =>
  sendWwMessage({
    "type": "candidates",
    "year": year
  }).then(response => {
    if (response.type === "candidates") {
      return response.candidatesByElectorate
    }
    return Promise.reject(`Unexpected response: ${JSON.stringify(response)}. I expected a "candidates" response.`);
  });

export const fetchCountback = (
  year: number,
  electorate: string,
  candidateToResign: string,
  candidatesToContest: string[],
  callback: (result: {
    message: string,
    newParagraph?: boolean
  } | null) => void
) =>
  sendSwMessageMultipart(
    (response) => {
      if (response.type === "countback") {
        callback(response);
      } else if (response.type === "end") {
        callback(null);
      } else {
        callback(null);
        throw new Error(`Unexpected response: ${JSON.stringify(response)}. I expected a "countback" response.`)
      }
    },
    {
      "type": "countback",
      "year": year,
      "electorate": electorate,
      "candidateToResign": candidateToResign,
      "candidatesToContest": candidatesToContest
    }
  )
