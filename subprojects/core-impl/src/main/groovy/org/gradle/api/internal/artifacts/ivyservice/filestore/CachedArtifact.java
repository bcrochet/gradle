/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.artifacts.ivyservice.filestore;

import java.io.File;

public interface CachedArtifact {
    String getSha1();

    /**
     * @throws CachedArtifactInvalidatedException if the cached artifact has been deleted or changed underneath us.
     */
    void copyTo(File destination);

    // TODO:DAZ Get rid of this, and use copyTo(), which will be safe for caches that may change during use.
    File getOrigin();
}