// Custom updater for pom.xml to be used with standard-version
// This allows standard-version to update Maven version in pom.xml

const xmlParser = require('xml2js');

module.exports.readVersion = function (contents) {
  const parser = new xmlParser.Parser();
  let version = null;

  parser.parseString(contents, function (err, result) {
    if (err) {
      throw err;
    }
    if (result.project && result.project.version && result.project.version[0]) {
      version = result.project.version[0];
    }
  });

  return version;
};

module.exports.writeVersion = function (contents, version) {
  // Simple regex replacement for Maven version
  // This works for most standard pom.xml structures
  const versionRegex = /<version>([^<]+)<\/version>/;
  const match = contents.match(versionRegex);

  if (match) {
    return contents.replace(versionRegex, `<version>${version}</version>`);
  }

  throw new Error('Could not find version in pom.xml');
};
