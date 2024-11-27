List of Contents
1. Introduction
2. Project Overview
3. App Overview
4. License

# Introduction
RF scanner project consists of three repositories.
- Main repository with Android app code (this one)
- [Server repository](https://github.com/MaybeAshLately/server) (Arduino) on account MaybeAshLately
- [Node repository](https://github.com/MaybeAshLately/node) (Arduino) on account MaybeAshLately



## Project Overview
The aim of this project was to create a system to measure radio frequency occupancy in the 2.4 GHz unlicensed band. It consists of:
- End devices (nodes)
- A server to collect their measurements.

The server saves measurements to an SD card and, on demand, sends them to a phone via Bluetooth.

Measurements have the format of an array of 126 8-bit integers, each representing the number of signals detected on the corresponding channel. The scanner operates in the 2.4 GHz to 2.525 GHz range, with each channel being 1 MHz wide. It can detect signals such as Wi-Fi and Bluetooth.

For detailed descriptions of the Node and Server components, please refer to their respective repositories.


# App overview
## Requirements
App is dedicated for API 31. To work it requires bluetooth and localization permissions.

Before using the app you have to pair with server using phone's bluetooth interface - pair with device named "RF_SCANNER_SERVER".

## Features

The app allows you to:
- download list of end nodes (along with their addresses),
- turn off the alarm,
- change critical level of signals that causes alarm,
- download last measurement (it is also remembered in phone app),
- download historic measurements (using previous and next buttons),
- clear historic data of node.

# License
This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see https://www.gnu.org/licenses/.
