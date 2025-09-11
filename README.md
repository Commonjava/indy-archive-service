# Indy Archive Service

Archive service for Indy artifact repository manager - caches build dependencies as local archives to accelerate similar future builds.

## Overview

The Indy Archive Service cooperates with the sidecar that enhances build performance by creating local archives of build dependencies during the build process. These archives serve as cached dependency sets that can significantly accelerate subsequent builds with similar dependency requirements.

Unlike traditional artifact repository archiving, this service focuses on build-time optimization rather than long-term storage management. It works in conjunction with the Indy Sidecar framework to provide intelligent dependency caching and retrieval mechanisms.

## Key Problems Solved

### Build Performance Optimization

Issue: Builds spend significant time downloading dependencies from remote repositories

Solution: Cache dependency sets locally as archives for rapid retrieval in similar builds

### Network Dependency Reduction

Issue: Builds are vulnerable to network issues and remote repository outages

Solution: Local archive caches reduce dependency on external network connectivity

### Bandwidth Optimization

Issue: Repeated downloads of the same dependencies consume network bandwidth

Solution: Single download, multiple reuse through archive caching

## Features

### Dependency Archiving

Build-Time Archiving: Automatically creates dependency archives during successful builds

Smart Packaging: Packages all resolved dependencies into optimized archive formats

### Archive Management

Archive Indexing: Maintains searchable index of available dependency archives

Archive Contents Validation: Validates archive contents integrity and validity

Storage Optimization: Efficiently stores archives with compression and deduplication

## Sidecar / Archive Integration Architecture Overview

Build Process ◄──▶ Indy Sidecar ◄──▶ Indy / Archive Service (LFS)

## Integration Workflow

Build Initiation: Build process sends dependency requirements to Indy Sidecar

Routing Decision: Sidecar routes request based on archive availability

Archive Path: If suitable archive content exists → Archive Service provides cached dependencies

Traditional Path: If no archive content exists → Indy Core resolves dependencies traditionally

## Build & Deploy

### dev mode

```shell script
./mvnw compile quarkus:dev
```

### native executable

```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

## Related Projects

[Indy](https://github.com/Commonjava/indy)

[Indy Sidecar](https://github.com/Commonjava/indy-sidecar)

