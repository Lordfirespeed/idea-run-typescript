# Intellij TypeScript Run Configuration

![Build](https://github.com/Lordfirespeed/intellij-typescript-run-configuration/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

## Description

<!-- Plugin description -->

This IntelliJ Platform plugin adds a run configuration template for TypeScript.

### Main Features

- Allows 'Current File' run configuration for TypeScript (`.ts`) files
- Supports TypeScript scratch files
- Supports selecting a TypeScript project file (`.tsconfig`)
- Supports debugging

<!-- Plugin description end -->

## Dependencies

Bundled JetBrains Platform plugins:
- [JavaScript and TypeScript](https://plugins.jetbrains.com/plugin/22069-javascript-and-typescript)
- [NodeJS](https://plugins.jetbrains.com/plugin/6098-nodejs)
- [JavaScript Debugger](https://plugins.jetbrains.com/plugin/17562-javascript-debugger)

## Installation

Install via JetBrains plugin marketplace, either in-browser or in your JetBrains IDE.

## About

This project is largely a rewrite of @bluelovers' [`idea-run-typescript` plugin](https://github.com/bluelovers/idea-run-typescript).

Some implementation mirrors JetBrains' `npm`, `node` and `jest` Run Configuration implementations @ Intellij Platform build 222.

Credit to [`intellij-rust`](https://github.com/intellij-rust/intellij-rust) for build scripts and project structure.

Icon made by [Freepik](https://www.freepik.com/) is sourced from [www.flaticon.com](https://www.flaticon.com/)
and licensed by [Creative Commons 3.0](https://creativecommons.org/licenses/by/3.0/)

