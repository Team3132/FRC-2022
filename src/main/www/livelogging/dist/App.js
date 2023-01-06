import {
  ChevronDownIcon,
  HamburgerIcon,
  LinkIcon,
  MoonIcon,
  SunIcon
} from "../_snowpack/pkg/@chakra-ui/icons.js";
import {
  Button,
  Center,
  Checkbox,
  Code,
  Drawer,
  DrawerBody,
  DrawerCloseButton,
  DrawerContent,
  DrawerFooter,
  DrawerHeader,
  DrawerOverlay,
  Flex,
  FormControl,
  FormLabel,
  Heading,
  IconButton,
  Input,
  Select,
  Spacer,
  Spinner,
  Stack,
  Text,
  useColorMode,
  useDisclosure
} from "../_snowpack/pkg/@chakra-ui/react.js";
import {uniqueId} from "../_snowpack/pkg/lodash.js";
import React, {useEffect, useRef, useState} from "../_snowpack/pkg/react.js";
import useWebSocket, {ReadyState} from "../_snowpack/pkg/react-use-websocket.js";
import {Virtuoso} from "../_snowpack/pkg/react-virtuoso.js";
import {useDebounce, useLocalStorage} from "../_snowpack/pkg/usehooks-ts.js";
import {filterByLogLevel} from "./utils.js";
function App() {
  const {colorMode, toggleColorMode} = useColorMode();
  const [autoscrollEnabled, setAutoscroll] = useLocalStorage("autoscroll", true);
  const [subsystems, setSubsystems] = useLocalStorage("subsystems", []);
  const [loglevel, setLoglevel] = useLocalStorage("loglevel", "Info");
  const virtuosoRef = useRef(null);
  const {isOpen, onOpen, onClose} = useDisclosure();
  const btnRef = React.useRef();
  const {lastMessage, readyState} = useWebSocket(`ws://${window.location.hostname}:5803`, {
    onError: (error) => {
      console.log(error);
    },
    share: true
  });
  const [newMessageHistory, setNewMessageHistory] = useState([]);
  const [searchString, setSearchString] = useState("");
  const deboucedSearchString = useDebounce(searchString, 500);
  const debouncedMessageHistory = useDebounce(newMessageHistory.filter((message) => filterByLogLevel(message, loglevel)).filter((message) => subsystems.filter((subsystem) => subsystem.enabled).some((subsystem) => subsystem.name === message.subsystem)).filter((message) => message.content.toLowerCase().includes(deboucedSearchString.toLowerCase())), 100);
  useEffect(() => {
    if (!lastMessage)
      return;
    var splitmsg = lastMessage.data.split(/^([0-9.]+) \((.+)\) \[([^]+)] (.*)/);
    var timestamp = splitmsg[1];
    var severity = splitmsg[2] ?? "Error";
    var subsystem = splitmsg[3] ?? "Unknown";
    var content = splitmsg[4] ?? lastMessage.data;
    var id = uniqueId();
    if (!subsystems.some((subsystemItem) => subsystem === subsystemItem.name)) {
      console.log(subsystem);
      setSubsystems((prevState) => prevState.concat({name: subsystem, enabled: true}));
    }
    setNewMessageHistory((prevState) => prevState.concat({
      timestamp,
      severity,
      subsystem,
      content,
      id
    }));
  }, [lastMessage]);
  const InnerItem = React.memo(({index}) => {
    if (!debouncedMessageHistory[index])
      return /* @__PURE__ */ React.createElement(React.Fragment, null, "Zero Size");
    const {id, timestamp, severity, subsystem, content} = debouncedMessageHistory[index];
    var color;
    var shade = colorMode === "dark" ? "300" : "500";
    switch (severity) {
      case "Info":
        color = "blue";
        break;
      case "Debug":
        color = "green";
        break;
      case "Error":
        color = "red";
        break;
      case "Warning":
        color = "yellow";
    }
    return /* @__PURE__ */ React.createElement(Text, {
      textColor: `${color}.${shade}`,
      w: "100%",
      id
    }, timestamp, " (", severity, ") [", subsystem, "] ", content);
  });
  const itemContent = (index) => {
    return /* @__PURE__ */ React.createElement(InnerItem, {
      index
    });
  };
  return /* @__PURE__ */ React.createElement(Flex, {
    h: "100vh",
    direction: "column"
  }, /* @__PURE__ */ React.createElement(Flex, null, /* @__PURE__ */ React.createElement(Center, {
    pl: 4
  }, /* @__PURE__ */ React.createElement(Heading, {
    size: "lg"
  }, "Robot Logs"), debouncedMessageHistory.length === newMessageHistory.filter((message) => filterByLogLevel(message, loglevel)).filter((message) => subsystems.filter((subsystem) => subsystem.enabled).some((subsystem) => subsystem.name === message.subsystem)).filter((message) => message.content.toLowerCase().includes(deboucedSearchString.toLowerCase())).length ? /* @__PURE__ */ React.createElement(React.Fragment, null) : /* @__PURE__ */ React.createElement(Spinner, {
    m: 2
  })), /* @__PURE__ */ React.createElement(Spacer, null), /* @__PURE__ */ React.createElement(Drawer, {
    isOpen,
    placement: "right",
    onClose,
    finalFocusRef: btnRef
  }, /* @__PURE__ */ React.createElement(DrawerOverlay, null), /* @__PURE__ */ React.createElement(DrawerContent, null, /* @__PURE__ */ React.createElement(DrawerCloseButton, null), /* @__PURE__ */ React.createElement(DrawerHeader, null, "Settings"), /* @__PURE__ */ React.createElement(DrawerBody, null, /* @__PURE__ */ React.createElement(FormControl, null, /* @__PURE__ */ React.createElement(FormLabel, {
    htmlFor: "loglevel"
  }, "Log Level"), /* @__PURE__ */ React.createElement(Select, {
    onChange: (e) => {
      setLoglevel(e.target.value);
    },
    w: "10em",
    id: "loglevel"
  }, /* @__PURE__ */ React.createElement("option", {
    value: "Error"
  }, "Error"), /* @__PURE__ */ React.createElement("option", {
    value: "Warning"
  }, "Warning"), /* @__PURE__ */ React.createElement("option", {
    value: "Info"
  }, "Info"), /* @__PURE__ */ React.createElement("option", {
    value: "Debug"
  }, "Debug"))), /* @__PURE__ */ React.createElement(FormControl, null, /* @__PURE__ */ React.createElement(FormLabel, {
    htmlFor: "email"
  }, "Subsystems"), /* @__PURE__ */ React.createElement(Stack, null, subsystems.map((subsystem, index) => /* @__PURE__ */ React.createElement(Checkbox, {
    isChecked: subsystem.enabled,
    onChange: (e) => {
      setSubsystems((prevState) => {
        subsystems[index].enabled = e.target.checked;
        return subsystems;
      });
    }
  }, subsystem.name)))), /* @__PURE__ */ React.createElement(FormControl, null, /* @__PURE__ */ React.createElement(FormLabel, {
    htmlFor: "search"
  }, "Search"), /* @__PURE__ */ React.createElement(Input, {
    id: "search",
    placeholder: "Search query",
    onChange: (e) => setSearchString(e.target.value),
    value: searchString
  }))), /* @__PURE__ */ React.createElement(DrawerFooter, null, /* @__PURE__ */ React.createElement(Button, {
    variant: "outline",
    mr: 3,
    onClick: onClose
  }, "Cancel"), /* @__PURE__ */ React.createElement(Button, {
    colorScheme: "blue"
  }, "Save")))), /* @__PURE__ */ React.createElement(IconButton, {
    "aria-label": "autoscroll",
    icon: /* @__PURE__ */ React.createElement(ChevronDownIcon, null),
    onClick: () => setAutoscroll((prevState) => !prevState),
    colorScheme: autoscrollEnabled ? "blue" : void 0,
    m: 2
  }), /* @__PURE__ */ React.createElement(IconButton, {
    "aria-label": "color mode",
    icon: colorMode === "dark" ? /* @__PURE__ */ React.createElement(MoonIcon, null) : /* @__PURE__ */ React.createElement(SunIcon, null),
    onClick: toggleColorMode,
    m: 2
  }), /* @__PURE__ */ React.createElement(IconButton, {
    "aria-label": "connect",
    icon: /* @__PURE__ */ React.createElement(LinkIcon, null),
    m: 2,
    colorScheme: {
      [ReadyState.CONNECTING]: "yellow",
      [ReadyState.OPEN]: "green",
      [ReadyState.CLOSING]: "yellow",
      [ReadyState.CLOSED]: "red",
      [ReadyState.UNINSTANTIATED]: void 0
    }[readyState]
  }), /* @__PURE__ */ React.createElement(IconButton, {
    "aria-label": "drawer",
    icon: /* @__PURE__ */ React.createElement(HamburgerIcon, null),
    ref: btnRef,
    onClick: onOpen,
    m: 2
  })), /* @__PURE__ */ React.createElement(Code, {
    w: "100%",
    flex: "1",
    colorScheme: "blackAlpha"
  }, /* @__PURE__ */ React.createElement(Virtuoso, {
    totalCount: debouncedMessageHistory.length,
    itemContent,
    ref: virtuosoRef,
    followOutput: autoscrollEnabled ? "smooth" : false
  })));
}
export default App;
