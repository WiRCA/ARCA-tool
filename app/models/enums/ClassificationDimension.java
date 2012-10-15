package models.enums;

import utils.IdComparableModel;

/**
 * Created with IntelliJ IDEA.
 * User: jaffa
 * Date: 10/8/12
 * Time: 5:15 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ClassificationDimension {

	WHAT(1),
	WHERE(2);

	/**
	 * the value of the enum
	 */
	public Integer value;

	/**
	 * Basic constructor
	 */
	ClassificationDimension(Integer value) {
		this.value = value;
	}

	/**
	 * Get the ClassificationDimension vith value
	 * @param id of the status
	 * @return found StatusOfCause or null
	 */
	public static ClassificationDimension valueOf(Integer id) {
		for (ClassificationDimension status : ClassificationDimension.values()) {
			if (status.value.equals(id)) {
				return status;
			}
		}
		return null;
	}
}