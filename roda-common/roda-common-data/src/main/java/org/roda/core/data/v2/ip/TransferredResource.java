/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.Date;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;

public class TransferredResource implements IsIndexed {

  private static final long serialVersionUID = 1L;
  private String uuid;
  private String id;
  private String fullPath;
  private String relativePath;
  private String parentId;
  private List<String> ancestorsPaths;

  private long size;
  private Date creationDate;
  private String name;
  private boolean file;

  public String getParentPath() {
    return parentId;
  }

  public void setParentPath(String parentPath) {
    this.parentId = parentPath;
  }

  public boolean isFile() {
    return file;
  }

  public void setFile(boolean file) {
    this.file = file;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFullPath() {
    return fullPath;
  }

  public void setFullPath(String fullPath) {
    this.fullPath = fullPath;
  }

  public String getRelativePath() {
    return relativePath;
  }

  public void setRelativePath(String relativePath) {
    this.relativePath = relativePath;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public List<String> getAncestorsPaths() {
    return ancestorsPaths;
  }

  public void setAncestorsPaths(List<String> ancestorsPaths) {
    this.ancestorsPaths = ancestorsPaths;
  }

  @Override
  public String getUUID() {
    return uuid;
  }

  public void setUUID(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ancestorsPaths == null) ? 0 : ancestorsPaths.hashCode());
    result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
    result = prime * result + (file ? 1231 : 1237);
    result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
    result = prime * result + ((relativePath == null) ? 0 : relativePath.hashCode());
    result = prime * result + (int) (size ^ (size >>> 32));
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TransferredResource other = (TransferredResource) obj;
    if (ancestorsPaths == null) {
      if (other.ancestorsPaths != null)
        return false;
    } else if (!ancestorsPaths.equals(other.ancestorsPaths))
      return false;
    if (creationDate == null) {
      if (other.creationDate != null)
        return false;
    } else if (!creationDate.equals(other.creationDate))
      return false;
    if (file != other.file)
      return false;
    if (fullPath == null) {
      if (other.fullPath != null)
        return false;
    } else if (!fullPath.equals(other.fullPath))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (parentId == null) {
      if (other.parentId != null)
        return false;
    } else if (!parentId.equals(other.parentId))
      return false;
    if (relativePath == null) {
      if (other.relativePath != null)
        return false;
    } else if (!relativePath.equals(other.relativePath))
      return false;
    if (size != other.size)
      return false;
    if (uuid == null) {
      if (other.uuid != null)
        return false;
    } else if (!uuid.equals(other.uuid))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "TransferredResource [uuid=" + uuid + ", id=" + id + ", fullPath=" + fullPath + ", relativePath="
      + relativePath + ", parentId=" + parentId + ", ancestorsPaths=" + ancestorsPaths + ", size=" + size
      + ", creationDate=" + creationDate + ", name=" + name + ", file=" + file + "]";
  }

}
