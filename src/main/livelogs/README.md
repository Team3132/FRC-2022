# TDU Live Logging

This is a client-side application that receives log messages from the robot's websocket server and displays them in a legible format.

## Stack

The aim of this project was for it to both easy on the eyes and very functional. It uses [ReactJS](https://reactjs.org/) augmented with [TypeScript](https://www.typescriptlang.org/) for stateful updates and typed JavaScript respectively. For UI Components it uses [Chakra UI](https://chakra-ui.com/), a React-Component based UI library.

# Development

## Getting Started

> Development of this project requires [Yarn](https://yarnpkg.com/) which should be installed on the system before starting.

## Available Scripts

In the project directory (`src/main/livelogs`), you can run:

### `yarn`

Installs all the required dependancies for the project.

### `yarn start`

Runs the app in the development mode.
Open [http://localhost:8080](http://localhost:8080) to view it in the browser.

The page will reload if you make edits.
You will also see any lint errors in the console.

### `yarn build`

Builds the app for production to the `build` folder.

> In our code it should appear in the `www/livelogging` directory
> It correctly bundles React in production mode and optimizes the build for the best performance.

The build is minified and the filenames include the hashes.
Your app is ready to be deployed!

See the section about [deployment](https://facebook.github.io/create-react-app/docs/deployment) for more information.

## Deployment

By default Github actions will re-build the app whenever there's a change and create a new commit including the compiled code to be send to the robot. Therefore, you shouldn't need to compile the code in order to copy it onto the robot as it's already included.

Running `www/copyLoggingFiles.sh` copies all the files onto the robot's logging USB.

## Learn More

You can learn more in the [React Snowpack](https://www.snowpack.dev/tutorials/react).

To learn React, check out the [React documentation](https://reactjs.org/).
