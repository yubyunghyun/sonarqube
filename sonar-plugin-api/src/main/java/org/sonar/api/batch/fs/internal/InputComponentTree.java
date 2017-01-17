package org.sonar.api.batch.fs.internal;

import java.util.Collection;

import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputModule;

public interface InputComponentTree {
  public InputModule root();

  public Collection<InputComponent> getChildren(InputComponent module);

  public InputComponent getParent(InputComponent module);
}
