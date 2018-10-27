/**
 *
 * $Id$
 */
package net.opengis.wfs.validation;

import org.eclipse.emf.common.util.EList;

/**
 * A sample validator interface for {@link net.opengis.wfs.DescribeFeatureTypeType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface DescribeFeatureTypeTypeValidator {
  boolean validate();

  boolean validateTypeName(EList value);
  boolean validateOutputFormat(String value);
}