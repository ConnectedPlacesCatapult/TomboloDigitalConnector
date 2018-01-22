# Contributing to Tombolo Digital Connector

Thank you for choosing to contribute to this project. **We need your superpowers!**  
To learn more about the Tombolo vision checkout our [website](http://www.tombolo.org.uk/).

Before starting to contribute have a look at the [README](README.md) and [docs](documentation). Our suggestion is to 
start with the [system architecture](documentation/System-Architecture.md) and [local datastore](documentation/Local-Datastore.md), and 
eventually go further in the documentation with other topics of your interest.

## Help wanted
[Open Source Community - Digital Connector](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/milestone/22)
milestone contains issues we think are interesting for you to work on. You can find them also through 
[`help wanted`](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/issues?q=is%3Aopen+is%3Aissue+label%3A%22help+wanted%22) 
tag. Feel free to check the other milestones in case you find something that excites you more.
If none of the issues reflects your problem/feature create a new one following the 
[issue template](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/.github/ISSUE_TEMPLATE.md).
Include screenshots and error log if available. 

## In progress
Chosen the issue you want to work on, let us know by tagging one of the project maintainers: *@thanosbnt*, 
*@sassalley*, *@lorenaqendro*, *@arya-hemanshu* and change the issue tag from `help wanted` to `in progress`. Regular
 update on its status is much appreciated, so that everyone knows its proceeding and eventually help.

## Choosing an issue and start contributing
If you are new to github you might want to read more about [issues](https://help.github.com/articles/about-issues/)
and [pull requests](https://help.github.com/articles/about-pull-requests/). 
To contribute to the project follow these steps:
- [Fork the repository](https://help.github.com/articles/fork-a-repo/).
- [Choose the issue]((https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/milestone/22)) you want to start 
working on, change its tag to `in progress` and tag a project maintainer to inform about it.
- Use the Github issue to discuss on the requirements, ask for more information, update on the progress or upload 
images related to the issue.
- Create a pull request when you feel you have addressed the issue.

## Submitting a Pull Request (PR)
When you are happy with your solution you can create a PR so we can review your contribution. Before submitting 
make sure that:
- If you are adding new functionality, create a unit test for it or update the existing tests when applicable.
- All of your commits are atomic (one feature per commit).
- Write a clear log message for the commit explaining the changes included in it.
  ```bash
  git commit -m "Summary description of the changes"
  > Detailed list if many
  > Detailed paragraph if the change is big and needs further explanation"
  ```
- Run all the tests locally in your IDE or via `gradle test`.
- Check the Wercker CLI pipeline succeeded with your last changes.
- You have merged the last version of master into your branch.

Now you are ready to open a PR by following this considerations:
- Include a description of your changes and approach taken, if many create a list of changes.
- Reference the issue number you are addressing.
- Keep it brief and clear. We won't complain about short PRs. Promise! ;)
- Assign it for a review to a project owner.

## Unit tesing
We would like to make sure that the Digital Connector works properly and if any bugs are introduced figure it out as 
soon as possible. We know that a full test coverage is not realistic, but we'd like to cover as much as we can. 
Therefore we will accept only PRs that include unit tests for new features or alterations of existing ones when needed.
A reminder of this is included in the checklist of the PR template.
  
We use the [JUnit](http://junit.org/junit5/) framework. If you are new to it, you can find a usage guide 
[here](http://www.vogella.com/tutorials/JUnit/article.html) as well as in action in the project test directory 
[src/test/java](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/test).

## Testing
Wercker CLI is configured to run tests on GitHub, so when you open a PR the test suite will run automatically as a build
 which you can view in the merge box of the pull request. You can see the test output by clicking the `Details` link 
 next to the build.

To run the test suite locally, use `gradle test` from the project root. If you use IntelliJ JUnit test runner, find the 
configuration [here](README.md#Run-tests).

## Conventions
We don't like to impose strict code conventions, but we must think about the people that will read and eventually 
change it in the future. Try to follow as much as possible the 
 [Java code conventions](http://www.oracle.com/technetwork/java/codeconvtoc-136057.html), you can't go wrong with 
 them. As a general rule read the suggested conventions, follow what's already embedded in the code and most 
 importantly, use common sense.  The following guidelines suggest some minimal effort to write a clear and readable 
 code.  

- Code
  - Indent with 4 spaces, not tabs.
  - No trailing whitespace. Blank lines should not have empty spaces.
  - End a file with a newline.
  - Use `a = b` and not `a=b`, `x += 1` and not `x+=1`, ...
  - Use `my_method(my_arg)` not `my_method( my_arg )`.
  - Prefer `method { implementation }` instead of `method{implementation}` for single-line blocks.
  - ALWAYS `if(condition) { do_something; }` not `if(condition) do_something;` for single-line blocks.
  - Javadoc comments should be add to every method, with parameters and return statements to explain their purpose.
  - Javadoc comments should be add to every class explaining its purpose.
  - Never commit anything with `System.out.print*()`. Use the proper logging procedure via 
  `Log4j: Logger.getLogger(MyClass.class)`.
  - Prefer not to commit `TODO`s. It's better to discuss in the issue where work by other members and future 
  implementations should go and eventually translate them in an issue.
 
- Structure
  - Package names should be all lowercase, no spaces and be descriptive of what they contain.
  - Packages should follow the url format (`tombolo.org.uk` => `uk.org.tombolo`).
  - Packages should be used to "group" similar objects together, their names should guide where a new class should be 
  places upon creation.
  - For importers: create a new package for each provider, both for source and test.
  - For fields: create a new package to organise a new type of field or use existing ones accordingly.
