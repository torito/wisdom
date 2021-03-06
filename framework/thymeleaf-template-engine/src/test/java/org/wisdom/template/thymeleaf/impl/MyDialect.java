/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.template.thymeleaf.impl;

import com.google.common.collect.ImmutableSet;
import org.thymeleaf.Arguments;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.attr.AbstractTextChildModifierAttrProcessor;

import java.util.Set;

public class MyDialect extends AbstractDialect {

    @Override
    public String getPrefix() {
        return "my";
    }

    @Override
    public Set<IProcessor> getProcessors() {
        return ImmutableSet.<IProcessor>of(new SayToAttrProcessor());
    }


    private class SayToAttrProcessor
            extends AbstractTextChildModifierAttrProcessor {


        public SayToAttrProcessor() {
            super("sayto");
        }


        public int getPrecedence() {
            return 10000;
        }


        //
        // Our processor is a subclass of the convenience abstract implementation
        // 'AbstractTextChildModifierAttrProcessor', which takes care of the
        // DOM modifying stuff and allows us just to implement this 'getText(...)'
        // method to compute the text to be set as tag body.
        //
        @Override
        protected String getText(final Arguments arguments, final Element element,
                                 final String attributeName) {
            return "Hello, "  + element.getAttributeValue(attributeName) + "!";
        }


    }
}
