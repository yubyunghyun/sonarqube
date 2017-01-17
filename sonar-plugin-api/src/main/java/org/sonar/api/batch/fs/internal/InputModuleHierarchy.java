package org.sonar.api.batch.fs.internal;

import java.util.List;

import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.fs.internal.DefaultInputModule;

public interface InputModuleHierarchy {
  DefaultInputModule root();
  
  boolean isRoot(InputModule module);

  List<DefaultInputModule> children(InputModule module);

  DefaultInputModule parent(InputModule module);
}
