import { Select as AntdSelect, SelectProps } from "antd";
import styled from "styled-components";
import * as React from "react";

export const ResultLine = styled.p<{ newParagraph: boolean }>`
  font-family: monospace;
  margin: ${(props) => props.newParagraph ? `1em 0 0` : `0`};
`;

export const Container = styled.div`
  overflow: auto;
  max-height: 100%;
  padding: 10px;
`;

export const Select = styled(AntdSelect)`
  .ant-select-selection-placeholder {
    color: black;
  }
` as unknown as React.FC<SelectProps>;