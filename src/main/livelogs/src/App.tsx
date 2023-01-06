import {
  ChevronDownIcon,
  HamburgerIcon,
  LinkIcon,
  MoonIcon,
  SunIcon,
} from '@chakra-ui/icons';
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
  useDisclosure,
} from '@chakra-ui/react';
import type * as CSS from 'csstype';
import { uniqueId } from 'lodash';
import React, { useEffect, useRef, useState } from 'react';
import useWebSocket, { ReadyState } from 'react-use-websocket';
import { Virtuoso } from 'react-virtuoso';
import { useDebounce, useLocalStorage } from 'usehooks-ts';
import { filterByLogLevel, LogLevels } from './utils';
function App() {
  /**
   * Defines the current color mode for theming purposes. e.g. dark or light mode.
   */
  const { colorMode, toggleColorMode } = useColorMode();

  /**
   * The local storage hook to determine if autoscroll is enabled and store it's state.
   */
  const [autoscrollEnabled, setAutoscroll] = useLocalStorage(
    'autoscroll',
    true,
  );

  /**
   * Subsystems
   */
  const [subsystems, setSubsystems] = useLocalStorage<
    { name: string; enabled: boolean }[]
  >('subsystems', []);

  /** Log Level */
  const [loglevel, setLoglevel] = useLocalStorage<LogLevels>(
    'loglevel',
    'Info',
  );

  /**
   * The reference so that the scroll location can be changed.
   */
  const virtuosoRef = useRef(null);

  /**
   * Handle the opening and closing of the drawer.
   */
  const { isOpen, onOpen, onClose } = useDisclosure();

  /**
   * Button ref for the drawer component.
   */
  const btnRef = React.useRef();

  /**
   * The websocket hook for state changes.
   */
  const { lastMessage, readyState } = useWebSocket(
    `ws://${window.location.hostname}:5803`,
    {
      onError: (error) => {
        console.log(error);
      },
      share: true,
    },
  );

  /**
   * React state that stores messages.
   */
  const [newMessageHistory, setNewMessageHistory] = useState<
    {
      timestamp: number;
      severity: string;
      subsystem: string;
      content: string;
      id: string;
    }[]
  >([]);

  /**
   * Search string for filtering.
   */
  const [searchString, setSearchString] = useState<string>('');

  /**
   * Debouced search string.
   */
  const deboucedSearchString = useDebounce(searchString, 500);

  /**
   * Debounced message history to prevent too many consecutive UI updates.
   */
  const debouncedMessageHistory = useDebounce(
    newMessageHistory
      .filter((message) => filterByLogLevel(message, loglevel))
      .filter((message) =>
        subsystems
          .filter((subsystem) => subsystem.enabled)
          .some((subsystem) => subsystem.name === message.subsystem),
      )
      .filter((message) =>
        message.content
          .toLowerCase()
          .includes(deboucedSearchString.toLowerCase()),
      ),
    100,
  );

  useEffect(() => {
    // In development there are duplicate messages displayed, this is not the case after build
    if (!lastMessage) return;
    var splitmsg = lastMessage.data.split(/^([0-9.]+) \((.+)\) \[([^]+)] (.*)/);
    var timestamp: number = splitmsg[1];
    var severity: string = splitmsg[2] ?? 'Error';
    var subsystem: string = splitmsg[3] ?? 'Unknown';
    var content: string = splitmsg[4] ?? lastMessage.data;
    var id = uniqueId();

    if (!subsystems.some((subsystemItem) => subsystem === subsystemItem.name)) {
      console.log(subsystem);
      setSubsystems((prevState) =>
        prevState.concat({ name: subsystem, enabled: true }),
      );
    }

    // setSubsystems((prevstate) =>
    //   prevstate.concat({ name: subsystem, enabled: true }),
    // );

    setNewMessageHistory((prevState) =>
      prevState.concat({
        timestamp,
        severity,
        subsystem,
        content,
        id,
      }),
    );
  }, [lastMessage]);

  /**
   * The reusable row (containing a single line from the log) for virtuoso. Virtuoso dynamically fetches the data for every new Row of items as they are made visible. This row defines how said row should look and where the data should be fetched from.
   * @param index The index of the current displayed message
   * @returns UI message component to be displayed.
   */

  const InnerItem: React.FC<{ index: number }> = React.memo(({ index }) => {
    if (!debouncedMessageHistory[index]) return <>Zero Size</>;
    const { id, timestamp, severity, subsystem, content } =
      debouncedMessageHistory[index];
    var color: CSS.Property.Color;
    var shade = colorMode === 'dark' ? '300' : '500';

    switch (severity) {
      case 'Info':
        color = 'blue';
        break;
      case 'Debug':
        color = 'green';
        break;
      case 'Error':
        color = 'red';
        break;
      case 'Warning':
        color = 'yellow';
    }

    return (
      <Text textColor={`${color}.${shade}`} w="100%" id={id}>
        {timestamp} ({severity}) [{subsystem}] {content}
      </Text>
    );
  });

  const itemContent = (index) => {
    return <InnerItem index={index} />;
  };

  /**
   * Main render for the app.
   */
  return (
    <Flex h="100vh" direction="column">
      <Flex>
        <Center pl={4}>
          <Heading size={'lg'}>Robot Logs</Heading>

          {debouncedMessageHistory.length ===
          newMessageHistory
            .filter((message) => filterByLogLevel(message, loglevel))
            .filter((message) =>
              subsystems
                .filter((subsystem) => subsystem.enabled)
                .some((subsystem) => subsystem.name === message.subsystem),
            )
            .filter((message) =>
              message.content
                .toLowerCase()
                .includes(deboucedSearchString.toLowerCase()),
            ).length ? (
            <></>
          ) : (
            <Spinner m={2} />
          )}
        </Center>
        <Spacer />

        <Drawer
          isOpen={isOpen}
          placement="right"
          onClose={onClose}
          finalFocusRef={btnRef}
        >
          <DrawerOverlay />
          <DrawerContent>
            <DrawerCloseButton />
            <DrawerHeader>Settings</DrawerHeader>
            <DrawerBody>
              <FormControl>
                <FormLabel htmlFor="loglevel">Log Level</FormLabel>
                <Select
                  onChange={(e) => {
                    setLoglevel(e.target.value as LogLevels);
                  }}
                  w={'10em'}
                  id="loglevel"
                >
                  <option value="Error">Error</option>
                  <option value="Warning">Warning</option>
                  <option value="Info">Info</option>
                  <option value="Debug">Debug</option>
                </Select>
              </FormControl>
              <FormControl>
                <FormLabel htmlFor="email">Subsystems</FormLabel>
                <Stack>
                  {subsystems.map((subsystem, index) => (
                    <Checkbox
                      isChecked={subsystem.enabled}
                      onChange={(e) => {
                        setSubsystems((prevState) => {
                          subsystems[index].enabled = e.target.checked;
                          return subsystems;
                        });
                      }}
                    >
                      {subsystem.name}
                    </Checkbox>
                  ))}
                </Stack>
              </FormControl>
              <FormControl>
                <FormLabel htmlFor="search">Search</FormLabel>
                <Input
                  id="search"
                  placeholder="Search query"
                  onChange={(e) => setSearchString(e.target.value)}
                  value={searchString}
                />
              </FormControl>
            </DrawerBody>
            <DrawerFooter>
              <Button variant="outline" mr={3} onClick={onClose}>
                Cancel
              </Button>
              <Button colorScheme="blue">Save</Button>
            </DrawerFooter>
          </DrawerContent>
        </Drawer>

        <IconButton
          aria-label="autoscroll"
          icon={<ChevronDownIcon />}
          onClick={() => setAutoscroll((prevState) => !prevState)}
          colorScheme={autoscrollEnabled ? 'blue' : undefined}
          m={2}
        />
        <IconButton
          aria-label="color mode"
          icon={colorMode === 'dark' ? <MoonIcon /> : <SunIcon />}
          onClick={toggleColorMode}
          m={2}
        />
        <IconButton
          aria-label="connect"
          icon={<LinkIcon />}
          m={2}
          colorScheme={
            {
              [ReadyState.CONNECTING]: 'yellow',
              [ReadyState.OPEN]: 'green',
              [ReadyState.CLOSING]: 'yellow',
              [ReadyState.CLOSED]: 'red',
              [ReadyState.UNINSTANTIATED]: undefined,
            }[readyState]
          }
        />
        <IconButton
          aria-label="drawer"
          icon={<HamburgerIcon />}
          ref={btnRef}
          onClick={onOpen}
          m={2}
        />
      </Flex>
      <Code w="100%" flex="1" colorScheme={'blackAlpha'}>
        <Virtuoso
          totalCount={debouncedMessageHistory.length}
          itemContent={itemContent}
          ref={virtuosoRef}
          followOutput={autoscrollEnabled ? 'smooth' : false}
        />
      </Code>
    </Flex>
  );
}

export default App;
