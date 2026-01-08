package com.jadaptive.api.plugins;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

public final class GAV {

	public final static class Builder {
		private Optional<String> repository = Optional.empty();
		private Optional<String> groupId = Optional.empty();
		private String artifactId;
		private Optional<String> version = Optional.empty();
		private Optional<String> classifier = Optional.empty();
		private Optional<String> type = Optional.empty();

		public Builder() {
		}

		public Builder withArtifactId(String artifactId) {
			this.artifactId = artifactId;
			return this;
		}

		public Builder ofProperties(Properties properties) {
			groupId = Optional.ofNullable(properties.getProperty("groupId"));
			artifactId = Optional.ofNullable(properties.getProperty("artifactId"))
					.orElseThrow(() -> new IllegalArgumentException("No artifact ID"));
			classifier = Optional.ofNullable(properties.getProperty("classifier "));
			version = Optional.ofNullable(properties.getProperty("version"));
			type = Optional.ofNullable(properties.getProperty("type"));
			return this;
		}

		public Builder ofSpec(String str) {
			return ofParts(str.split(":"));
		}

		public Builder ofParts(Collection<String> parts) {
			try {
				var it = parts.iterator();
				var first = it.next();
				if (first.startsWith("@")) {
					withRepository(first.substring(1));
					it.next();
				} else {
					withGroupId(first);
				}
				withArtifactId(it.next());
				if (it.hasNext())
					withVersion(it.next());
				if (it.hasNext())
					withClassifier(it.next());
				if (it.hasNext())
					withType(it.next());
			} catch (NoSuchElementException nse) {
				throw new IllegalArgumentException(MessageFormat.format(
						"Truncated GAV string ''{0}'', must be in format [@repository]:groupId:artifactId[:version][:classifier][:type]. groupId may be empty, i.e. '':artifactId:...'' or ''repository::artifact:...''",
						String.join(":", parts)));
			}

			return this;
		}

		public Builder ofParts(String... parts) {
			return ofParts(Arrays.asList(parts));
		}

		public Builder withRepository(String repository) {
			return withRepository(repository.equals("") ? Optional.empty() : Optional.ofNullable(repository));
		}

		public Builder withRepository(Optional<String> repository) {
			this.repository = repository;
			return this;
		}

		public Builder withGroupId(String groupId) {
			return withGroupId(groupId.equals("") ? Optional.empty() : Optional.ofNullable(groupId));
		}

		public Builder withGroupId(Optional<String> groupId) {
			this.groupId = groupId;
			return this;
		}

		public Builder withVersion(String version) {
			return withVersion(version.equals("") ? Optional.empty() : Optional.ofNullable(version));
		}

		public Builder withVersion(Optional<String> version) {
			this.version = version;
			return this;
		}

		public Builder withClassifier(String classifier) {
			return withClassifier(classifier.equals("") ? Optional.empty() : Optional.ofNullable(classifier));
		}

		public Builder withClassifier(Optional<String> classifier) {
			this.classifier = classifier;
			return this;
		}

		public Builder withType(String type) {
			return withType(type.equals("") ? Optional.empty() : Optional.ofNullable(type));
		}

		public Builder withType(Optional<String> type) {
			this.type = type;
			return this;
		}

		public GAV build() {
			return new GAV(repository, groupId, artifactId, version, classifier, type, version.isPresent() && version.get().endsWith("-SNAPSHOT") ? version : Optional.empty());
		}
	}

	public final static GAV ofSpec(String spec) {
		return new Builder().ofSpec(spec).build();
	}

	public final static GAV ofParts(String... parts) {
		return new Builder().ofParts(parts).build();
	}

	public final static GAV ofProperties(Properties properties) {
		return new Builder().ofProperties(properties).build();
	}

	private final Optional<String> repository;
	private final Optional<String> groupId;
	private final String artifactId;
	private final Optional<String> version;
	private final Optional<String> classifier;
	private final Optional<String> type;
	private final Optional<String> snapshotVersion; 

	private GAV(Optional<String> repository, Optional<String> groupId, String artifactId, Optional<String> version,
			Optional<String> classifier, Optional<String> type, Optional<String> snapshotVersion) {
		super();
		this.repository = repository;
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.classifier = classifier;
		this.type = type;
		this.snapshotVersion = snapshotVersion;
	}

	public Optional<String> repositoryOr() {
		return repository;
	}

	public String repository() {
		return repository.orElseThrow(() -> new IllegalStateException("This GAV has no repository"));
	}

	public Optional<String> groupIdOr() {
		return groupId;
	}

	public String artifactId() {
		return artifactId;
	}

	public Optional<String> versionOr() {
		return version;
	}

	public Optional<String> classifierOr() {
		return classifier;
	}

	public String version() {
		return version.orElseThrow(() -> new IllegalStateException(MessageFormat.format("This GAV ''{0}'' has no version", this)));
	}

	public String classifier() {
		return classifier.orElseThrow(() -> new IllegalStateException(MessageFormat.format("This GAV ''{0}'' has no classifier", this)));
	}

	public Optional<String> typeOr() {
		return type;
	}

	public String type() {
		return type.orElseThrow(() -> new IllegalStateException(MessageFormat.format("This GAV ''{0}'' has no type", this)));
	}

	public String toString() {
		var b = new StringBuilder();
		repository.ifPresent((r) -> b.append("@" + r + ":"));
		b.append(groupId.orElse(""));
		b.append(":");
		b.append(artifactId);
		version.ifPresent(v -> {
			b.append(':');
			b.append(v);
			classifier.ifPresent(c -> {
				b.append(':');
				b.append(c);
			});
			type.ifPresent(c -> {
				b.append(':');
				b.append(c);
			});
		});
		return b.toString();
	}

	public String groupId() {
		return groupId.orElseThrow(() -> new IllegalStateException(MessageFormat.format("This GAV ''{0}'' has no group", this)));
	}

	@Override
	public int hashCode() {
		return Objects.hash(artifactId, classifier, type, groupId, repository, version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GAV other = (GAV) obj;
		return Objects.equals(artifactId, other.artifactId) && Objects.equals(classifier, other.classifier)
				 && Objects.equals(type, other.type)
				&& Objects.equals(groupId, other.groupId) && Objects.equals(repository, other.repository)
				&& Objects.equals(version, other.version);
	}

	public boolean hasVersion() {
		return version.isPresent();
	}

	public boolean hasGroupId() {
		return groupId.isPresent();
	}

	public boolean hasClassifier() {
		return classifier.isPresent();
	}

	public boolean hasType() {
		return type.isPresent();
	}

	public boolean hasRepository() {
		return repository.isPresent();
	}

	public GAV toWithoutVersion() {
		return new GAV(repository, groupId, artifactId, Optional.empty(), classifier, type, Optional.empty());
	}

	public GAV toWithoutRepository() {
		return new GAV(Optional.empty(), groupId, artifactId, version, classifier, type, snapshotVersion);
	}

	public boolean isSnapshot() {
		return snapshotVersion.isPresent();
	}

	public GAV toResolvedSnapshotVersion(String version) {
		return new GAV(repository, groupId, artifactId, this.version, classifier, type, Optional.of(version));
	}

	public String resolvedVersion() {
		return snapshotVersion.isEmpty() ? version() : snapshotVersion.get();
	}

}
