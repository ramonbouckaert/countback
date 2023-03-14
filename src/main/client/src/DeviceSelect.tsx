import React, { FunctionComponent } from "react";
import { isMobile } from "mobile-device-detect";
import { SelectProps } from "antd";
import { Select } from "./styles";

export const DeviceSelect: FunctionComponent<SelectProps> =
  (props) => {
    if (isMobile) {
      return (
        <>
          {props.mode === "multiple"
          ? (<div style={{ overflow: "hidden", whiteSpace: "nowrap" }}>{props.placeholder}</div>)
          : null}
          <select
            placeholder={props.placeholder?.toString() ?? undefined}
            multiple={props.mode === "multiple"}
            style={props.style}
            value={props.value}
            onChange={event => {
              if (props.onChange) {
                if (props.mode === "multiple") {
                  let result: string[] = [];
                  const options = event.target.options;
                  let i = 0, iLen = options.length;
                  for (; i < iLen; i++) {
                    let opt = options[i];
                    if (opt.selected) result.push(opt.value || opt.text)
                  }
                  props.onChange(result, result.map(r => ({
                    label: r
                  })));
                } else {
                  props.onChange(event.target.value, { label: event.target.value });
                }
              }
            }}
          >
            {props.loading
              ? (
                <optgroup>Loading...</optgroup>
              )
              : [
                ...props.mode === "multiple" ? [] : [<option disabled selected={true}
                                                             value={""}>{props.placeholder}</option>],
                ...(props.options ?? []).map(opt => (
                  <option
                    label={opt.label?.toString()}
                    value={opt.value?.toString()}
                    onClick={() => {
                      if (props.onSelect && opt.value) {
                        props.onSelect(opt.value, { label: opt.label })
                      }
                    }}
                  >
                    {opt.label?.toString()}
                  </option>
                ))
              ]
            }
          </select>
        </>
      );
    } else {
      return (
        <Select {...props} />
      );
    }
  }