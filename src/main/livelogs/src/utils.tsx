/**
 * Possible Selected Log Levels
 */
export type LogLevels = 'Debug' | 'Info' | 'Warning' | 'Error';

/**
 * Interface for log messages
 * @example
 * 100.00 (Info) [Climber] the climber is climbing
 */
export interface Message {
  timestamp: number;
  severity: string;
  subsystem: string;
  content: string;
  id: string;
}

/**
 * Filters the string by log level.
 * @ignore This function is currently unused and non-functional.
 *
 * @param severity Message from the websocket
 * @returns Boolean based on whether the log level is active
 */
export const filterByLogLevel = (
  message: Message,
  activelogLevel: LogLevels,
) => {
  const error = message.severity === 'Error';
  const warning = message.severity === 'Warning' || error;
  const info = message.severity === 'Info' || warning;
  const debug = message.severity === 'Debug' || info;

  switch (activelogLevel) {
    case 'Debug':
      return debug;
    case 'Info':
      return info;
    case 'Warning':
      return warning;
    case 'Error':
      return error;
  }
};

/**
 * Filters to content of a single message by it's content based on a search string.
 * @param message The log message object
 * @param filterString The search pattern to use
 * @returns true if the given message contains the given filter or false if the given message doesn't contain the search string.
 */
export const filterByMessageContent = (message: Message, filterString) => {
  var lowerCaseFilterString = filterString.toLowerCase();
  return message.content.toLowerCase().includes(lowerCaseFilterString);
};

/**
 * Filters the string by the active subsystems.
 *
 * @param subsystem Message from the websocket
 * @returns Boolean based on if active subsytem exists in message
 */
export const filterByActiveSubsystems = (
  message: Message,
  activeSubsystems,
) => {
  if (activeSubsystems.length !== 0) {
    return activeSubsystems
      .map((activeSubsystem) =>
        message.subsystem ? message.subsystem === activeSubsystem : false,
      )
      .some((item) => item);
  }
  return true;
};

/**
 * Gives a color based on the state of the websocket.
 * @returns Color value used by components.
 */
export const websocketStateColor = (websocketState) => {
  switch (websocketState) {
    case 'open':
      return 'green';
    case 'connecting':
      return 'yellow';
    case 'closed':
      return 'red';
    default:
      return undefined; // Defaults to the default button colour if none is defined.
  }
};

/**
 * Add and remove the subsystems from the active list depending on checkbox state.
 *
 * @param event The checkbox change event
 * @param subsystem The subsystem the checkbox belongs to
 */
export const setSubsystemActive = (
  event: React.ChangeEvent<HTMLInputElement>,
  subsystem: string | undefined,
  setActiveSubsystems,
) => {
  if (event.target.checked) {
    setActiveSubsystems((prevState) => [...prevState, subsystem]);
  } else {
    setActiveSubsystems((prevState) => [
      ...prevState.filter((item) => {
        return item !== subsystem;
      }),
    ]);
  }
};
