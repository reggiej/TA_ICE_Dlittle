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

import oracle.toplink.libraries.asm.Label;
import oracle.toplink.libraries.asm.Constants;
import oracle.toplink.libraries.asm.CodeVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A node that represents a TABLESWITCH instruction.
 * 
 * @author Eric Bruneton
 */

public class TableSwitchInsnNode extends AbstractInsnNode {

  /**
   * The minimum key value.
   */

  public int min;

  /**
   * The maximum key value.
   */

  public int max;

  /**
   * Beginning of the default handler block.
   */

  public Label dflt;

  /**
   * Beginnings of the handler blocks. This list is a list of {@link Label
   * Label} objects.
   */

  public final List labels;

  /**
   * Constructs a new {@link TableSwitchInsnNode TableSwitchInsnNode}.
   *
   * @param min the minimum key value.
   * @param max the maximum key value.
   * @param dflt beginning of the default handler block.
   * @param labels beginnings of the handler blocks. <tt>labels[i]</tt> is the
   *      beginning of the handler block for the <tt>min + i</tt> key.
   */

  public TableSwitchInsnNode (
    final int min,
    final int max,
    final Label dflt,
    final Label[] labels)
  {
    super(Constants.TABLESWITCH);
    this.min = min;
    this.max = max;
    this.dflt = dflt;
    this.labels = new ArrayList();
    if (labels != null) {
      this.labels.addAll(Arrays.asList(labels));
    }
  }

  public void accept (final CodeVisitor cv) {
    Label[] labels = new Label[this.labels.size()];
    this.labels.toArray(labels);
    cv.visitTableSwitchInsn(min, max, dflt, labels);
  }
}
