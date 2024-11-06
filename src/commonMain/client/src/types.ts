export type Candidate = {
    id: number,
    name: string
}

type CandidatesRequest = {
  type: "candidates"
  year: number
}

type CountbackRequest = {
  type: "countback"
  year: number,
  electorate: string,
  candidateToResign: number,
  candidatesToContest: number[]
}

export type WebWorkerRequest = CandidatesRequest | CountbackRequest

type ReadyResponse = {

  type: "ready",

  id: string
}

type ErrorResponse = {
  type: "error"
  id: string | null,
  message: string,
  stackTrace: string | null
}

type CandidatesResponse = {
  type: "candidates"
  id: string
  candidatesByElectorate: Record<string, Candidate[]>
}

type CountbackResponse = {
  type: "countback"
  id: string,
  message: string,
  newParagraph?: boolean
}

type EndResponse = {
  type: "end"
  id: string
}

export type WebWorkerResponse = ReadyResponse | ErrorResponse | CandidatesResponse | CountbackResponse | EndResponse