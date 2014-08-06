/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
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
 * #L%
 */
package org.wisdom.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Declares a requirements on a template.
 *
 * This annotation is handled by the iPOJO manipulator and is equivalent to:
 * <pre>{@code
 *          &#64;Requires(filter="(name=...)"
 * }</pre>
 *
 * The mapped requirement is scalar and mandatory.
 * As this annotation is read during the manipulation process, the <em>class</em> retention (default) is enough.
 */
@Target(ElementType.FIELD)
public @interface View {

    /**
     * Specifies the template name.
     */
    String value();
}
