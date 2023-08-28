# Gama Benchmark

## Description

This repo aims to benchmark Gama's performance on different use cases. Said benchmark gets executed on GitHub actions runners.

## Tools used

- [Gama](https://gama-platform.org/) is the simulation platform that will be benchmarked.
- GitHub actions to run the benchmark.
- Java.
- [R](https://www.r-project.org/) to generate the report from the results of the benchmark.

## Contents of the repo
This repo contains 3 things: 

- A Java tool to record the performance of an experiment run by attaching to Gama's JVM through JMX.
- A Gama workspace containing models that aims to benchmark certain use cases.
- The `experiment` directory that contains experiments to run during the benchmark.
- The `benchmark_targets.json` file that describes each use case (Iterating over a long List of agents, spatial models, etc...): the experiment that will be run, how many times they will be run and for each of them the size of input.
- A GitHub action that will run the benchmark on all models and give the results thanks to the `R/report.Rmd` rmarkdown file.


## Add a test to the benchmark

- Open Gama and enter the workspace
- Create a new Project if your test is related to another use case
- Create a new model having an experiment with only one parameter describing the size of the input.
- Generate an experiment input file using the [headless legacy mode](https://gama-platform.org/wiki/HeadlessLegacy), preferably in the `experiment` directory. Get rid of its `<Output>` tags as it causes crashes.
- Update the `benchmark_targets.json` file to add your test.

### The `benchmark_targets.json` file

Each entry is a use case with the following schema: 
- `useCase` is the name of the use case.
- `experimentsFiles` is an array of experiments to run.
- `experimentsFiles.filename` is the path to the experiment input file.
- `experimentsFiles.experimentName` is the name of the experiment, it will be used to group all results of the same experiment.
- `experimentsFiles.N` is the size of the input.
- `numberOfRuns` is the number of times the experiment will be run.

## Run the benchmark

The benchmark runs under GitHub actions on a `workflow_dispatch` event. Go to the Actions tab, click on `Benchmarking` and then launch it with the `Run workflow` button.

You can also use [act](https://github.com/nektos/act) to test it locally, though it will be REALLY slow since caching won't work, plus the artifact uploading won't work.

# Results

To get the results, go to the Actions tab, click on `Benchmarking` and then click on the latest run. You will find the results in the `Artifacts` section.
You will download a zip archive containing:

- The `results.csv` file containing the results of the benchmark.
- The `report.pdf` file containing the report generated by the R script.
- All the plots shown in the report. These plots show, for each use case, the average resources consumption depending on input size.  