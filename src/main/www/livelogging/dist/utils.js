export const filterByLogLevel = (message, activelogLevel) => {
  const error = message.severity === "Error";
  const warning = message.severity === "Warning" || error;
  const info = message.severity === "Info" || warning;
  const debug = message.severity === "Debug" || info;
  switch (activelogLevel) {
    case "Debug":
      return debug;
    case "Info":
      return info;
    case "Warning":
      return warning;
    case "Error":
      return error;
  }
};
export const filterByMessageContent = (message, filterString) => {
  var lowerCaseFilterString = filterString.toLowerCase();
  return message.content.toLowerCase().includes(lowerCaseFilterString);
};
export const filterByActiveSubsystems = (message, activeSubsystems) => {
  if (activeSubsystems.length !== 0) {
    return activeSubsystems.map((activeSubsystem) => message.subsystem ? message.subsystem === activeSubsystem : false).some((item) => item);
  }
  return true;
};
export const websocketStateColor = (websocketState) => {
  switch (websocketState) {
    case "open":
      return "green";
    case "connecting":
      return "yellow";
    case "closed":
      return "red";
    default:
      return void 0;
  }
};
export const setSubsystemActive = (event, subsystem, setActiveSubsystems) => {
  if (event.target.checked) {
    setActiveSubsystems((prevState) => [...prevState, subsystem]);
  } else {
    setActiveSubsystems((prevState) => [
      ...prevState.filter((item) => {
        return item !== subsystem;
      })
    ]);
  }
};
