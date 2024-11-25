/*
 * Copyright 2020-Present The Serverless Workflow Specification Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.serverlessworkflow.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

class FileResource implements StaticResource {

  private Path path;

  public FileResource(Path path) {
    this.path = path;
  }

  @Override
  public InputStream open() {
    try {
      return Files.newInputStream(path);
    } catch (IOException io) {
      throw new UncheckedIOException(io);
    }
  }

  @Override
  public String name() {
    return path.getFileName().toString();
  }
}
