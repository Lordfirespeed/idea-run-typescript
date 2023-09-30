<!-- Keep a Changelog guide -> https://keepachangelog.com -->
# `idea-run-typescript` Changelog

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
See [Conventional Commits](https://conventionalcommits.org) for commit guidelines.

## [Unreleased]

### ✨　Features

* tsx support!
* selecting a 'Project' ts execute package (via Preferences -> Languages + Frameworks -> TypeScript -> Execute)


### 🐛　Bug Fixes


### 📦　Code Refactoring

* package name `io.plugin.tsnode` -> `com.github.bluelovers.idea_ts_run_configuration`
* near full rewrite to use modern JetBrains API and remove references to deprecated members


### 🛠　Build System

* update to Gradle 8.2.1
* migrate from Groovy DSL to Kotlin DSL
* migrate from `buildscript` to `plugins`
* use a version catalog
* use Gradle tasks instead of JavaScript to patch XML

## [Past]
See [`bluelovers/idea-run-typescript`](https://github.com/bluelovers/idea-run-typescript/blob/3fc04b125b6494956ac6a7c150da935392410345/CHANGELOG.md)
