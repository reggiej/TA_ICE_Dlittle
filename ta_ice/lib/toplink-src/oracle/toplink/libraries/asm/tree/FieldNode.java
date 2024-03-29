/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000,2002,2003 INRIA, France Telecom 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package oracle.toplink.libraries.asm.tree;

import oracle.toplink.libraries.asm.ClassVisitor;
import oracle.toplink.libraries.asm.Attribute;

/**
 * A node that represents a field.
 * 
 * @author Eric Bruneton
 */

public class FieldNode {

  /**
   * The field's access flags (see {@link oracle.toplink.libraries.asm.Constants}). This
   * field also indicates if the field is synthetic and/or deprecated.
   */

  public int access;

  /**
   * The field's name.
   */

  public String name;

  /**
   * The field's descriptor (see {@link oracle.toplink.libraries.asm.Type Type}).
   */

  public String desc;

  /**
   * The field's initial value. This field, which may be <tt>null</tt> if the
   * field does not have an initial value, must be an {@link java.lang.Integer
   * Integer}, a {@link java.lang.Float Float}, a {@link java.lang.Long Long},
   * a {@link java.lang.Double Double} or a {@link String String}.
   */

  public Object value;

  /**
   * The non standard attributes of the field.
   */

  public Attribute attrs;

  /**
   * Constructs a new {@link FieldNode FieldNode} object.
   *
   * @param access the field's access flags (see {@link
   *      oracle.toplink.libraries.asm.Constants}). This parameter also indicates if the
   *      field is synthetic and/or deprecated.
   * @param name the field's name.
   * @param desc the field's descriptor (see {@link oracle.toplink.libraries.asm.Type
   *      Type}).
   * @param value the field's initial value. This parameter, which may be
   *      <tt>null</tt> if the field does not have an initial value, must be an
   *      {@link java.lang.Integer Integer}, a {@link java.lang.Float Float}, a
   *      {@link java.lang.Long Long}, a {@link java.lang.Double Double} or a
   *      {@link String String}.
   * @param attrs the non standard attributes of the field.
   */

  public FieldNode (
    final int access,
    final String name,
    final String desc,
    final Object value,
    final Attribute attrs)
  {
    this.access = access;
    this.name = name;
    this.desc = desc;
    this.value = value;
    this.attrs = attrs;
  }

  /**
   * Makes the given class visitor visit this field.
   *
   * @param cv a class visitor.
   */

  public void accept (final ClassVisitor cv) {
    cv.visitField(access, name, desc, value, attrs);
  }
}
