/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.painless.node;

import org.elasticsearch.painless.Locals;
import org.elasticsearch.painless.Location;
import org.elasticsearch.painless.ir.NullSafeSubNode;
import org.elasticsearch.painless.symbol.ScriptRoot;

import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Implements a call who's value is null if the prefix is null rather than throwing an NPE.
 */
public class PSubNullSafeCallInvoke extends AExpression {
    /**
     * The expression gaurded by the null check. Required at construction time and replaced at analysis time.
     */
    private AExpression guarded;

    public PSubNullSafeCallInvoke(Location location, AExpression guarded) {
        super(location);
        this.guarded = requireNonNull(guarded);
    }

    @Override
    void extractVariables(Set<String> variables) {
        throw createError(new IllegalStateException("illegal tree structure"));
    }

    @Override
    void analyze(ScriptRoot scriptRoot, Locals locals) {
        guarded.analyze(scriptRoot, locals);
        actual = guarded.actual;
        if (actual.isPrimitive()) {
            throw new IllegalArgumentException("Result of null safe operator must be nullable");
        }
    }

    @Override
    NullSafeSubNode write() {
        NullSafeSubNode nullSafeSubNode = new NullSafeSubNode();

        nullSafeSubNode.setChildNode(guarded.write());

        nullSafeSubNode.setLocation(location);
        nullSafeSubNode.setExpressionType(actual);

        return nullSafeSubNode;
    }

    @Override
    public String toString() {
        return singleLineToString(guarded);
    }
}
