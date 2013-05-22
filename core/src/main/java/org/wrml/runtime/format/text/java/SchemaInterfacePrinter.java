/**
 * WRML - Web Resource Modeling Language
 *  __     __   ______   __    __   __        
 * /\ \  _ \ \ /\  == \ /\ "-./  \ /\ \       
 * \ \ \/ ".\ \\ \  __< \ \ \-./\ \\ \ \____  
 *  \ \__/".~\_\\ \_\ \_\\ \_\ \ \_\\ \_____\ 
 *   \/_/   \/_/ \/_/ /_/ \/_/  \/_/ \/_____/ 
 *
 * http://www.wrml.org                                                   
 *
 * Copyright (C) 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org) 
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

package org.wrml.runtime.format.text.java;

import org.objectweb.asm.*;
import org.wrml.model.schema.Schema;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * TODO: Finish this class
 */
public class SchemaInterfacePrinter implements ClassVisitor
{

    private final Schema _Schema;

    private final PrintStream _PrintStream;

    public SchemaInterfacePrinter(final Schema schema, final OutputStream out)
    {

        _Schema = schema;

        // Autoflushing makes sense, yes?
        _PrintStream = new PrintStream(out, true);

    }

    public PrintStream getPrintStream()
    {

        return _PrintStream;
    }

    public Schema getSchema()
    {

        return _Schema;
    }

    @Override
    public void visit(final int version,
                      final int access,
                      final String name,
                      final String signature,
                      final String superName,
                      final String[] interfaces)
    {

        _PrintStream.println(name + " extends " + superName + " {");

    }

    @Override
    public void visitSource(final String s, final String s2)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visitOuterClass(final String s, final String s2, final String s3)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String s, final boolean b)
    {

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visitAttribute(final Attribute attribute)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visitInnerClass(final String s, final String s2, final String s3, final int i)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value)
    {

        _PrintStream.println(" " + desc + " " + name);
        return null;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions)
    {

        _PrintStream.println(" " + name + desc);
        return null;
    }

    @Override
    public void visitEnd()
    {

        _PrintStream.println("}");
    }
}


