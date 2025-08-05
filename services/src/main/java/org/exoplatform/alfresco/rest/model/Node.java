package org.exoplatform.alfresco.rest.model;

public class Node {
    private String id;
    private String name;
    private String nodeType;
    private boolean isFolder;
    private boolean isFile;
    private boolean isLocked = false;
    private String createdAt;
    private String modifiedAt;
    private String parentId;
    private boolean isLink;
    private boolean isFavorite;
    private boolean isDirectLinkEnabled;

    public Node() {
    }

    public Node(String id, String name, String nodeType, boolean isFolder, boolean isFile, boolean isLocked, String createdAt, String modifiedAt, String parentId, boolean isLink, boolean isFavorite, boolean isDirectLinkEnabled) {
        this.id = id;
        this.name = name;
        this.nodeType = nodeType;
        this.isFolder = isFolder;
        this.isFile = isFile;
        this.isLocked = isLocked;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.parentId = parentId;
        this.isLink = isLink;
        this.isFavorite = isFavorite;
        this.isDirectLinkEnabled = isDirectLinkEnabled;
    }

    // Static method to access the Builder
    public static Builder builder() {
        return new Builder();
    }

    // Builder class
    public static class Builder {
        private String id;
        private String name;
        private String nodeType;
        private boolean isFolder;
        private boolean isFile;
        private boolean isLocked = false;
        private String createdAt;
        private String modifiedAt;
        private String parentId;
        private boolean isLink;
        private boolean isFavorite;
        private boolean isDirectLinkEnabled;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder nodeType(String nodeType) {
            this.nodeType = nodeType;
            return this;
        }

        public Builder isFolder(boolean isFolder) {
            this.isFolder = isFolder;
            return this;
        }

        public Builder isFile(boolean isFile) {
            this.isFile = isFile;
            return this;
        }

        public Builder isLocked(boolean isLocked) {
            this.isLocked = isLocked;
            return this;
        }

        public Builder createdAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder modifiedAt(String modifiedAt) {
            this.modifiedAt = modifiedAt;
            return this;
        }

        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder isLink(boolean isLink) {
            this.isLink = isLink;
            return this;
        }

        public Builder isFavorite(boolean isFavorite) {
            this.isFavorite = isFavorite;
            return this;
        }

        public Builder isDirectLinkEnabled(boolean isDirectLinkEnabled) {
            this.isDirectLinkEnabled = isDirectLinkEnabled;
            return this;
        }

        public Node build() {
            return new Node(id, name, nodeType, isFolder, isFile, isLocked, createdAt, modifiedAt, parentId,
                    isLink, isFavorite, isDirectLinkEnabled);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public boolean isLink() {
        return isLink;
    }

    public void setLink(boolean link) {
        isLink = link;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isDirectLinkEnabled() {
        return isDirectLinkEnabled;
    }

    public void setDirectLinkEnabled(boolean directLinkEnabled) {
        isDirectLinkEnabled = directLinkEnabled;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", nodeType='" + nodeType + '\'' +
                ", isFolder=" + isFolder +
                ", isFile=" + isFile +
                ", isLocked=" + isLocked +
                ", createdAt='" + createdAt + '\'' +
                ", modifiedAt='" + modifiedAt + '\'' +
                ", parentId='" + parentId + '\'' +
                ", isLink=" + isLink +
                ", isFavorite=" + isFavorite +
                ", isDirectLinkEnabled=" + isDirectLinkEnabled +
                '}';
    }


}
