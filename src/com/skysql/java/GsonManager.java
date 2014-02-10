/*
 * This file is distributed as part of the MariaDB Enterprise. It is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * version 2.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright 2012-2014 SkySQL Ab
 */

package com.skysql.java;

import com.google.gson.Gson;

/**
 * Class to manage Json strings, from and to Java objects.
 * This class requires the definitions of appropriate classes
 * that reflect the Json structure.
 * 
 * @author Massimo Siani
 *
 */
public class GsonManager {

	/**
	 * Gson object.
	 */
	private static Gson			gson = new Gson();

	/**
	 * Generate Json from an object.
	 * 
	 * @param obj the object.
	 * @return the Json string.
	 */
	public static <T> String toJson(T obj) {
		String result = gson.toJson(obj);
		return result;
	}

	/**
	 * Convert a Json into a Java object. If the Object class is derived
	 * from GsonErrors and the object contains errors, this method
	 * prints the error and/or warning messages on the stderr. 
	 * 
	 * @param inJson the Json as a string.
	 * @param objClass the class of the object.
	 * @return the deserialized Json as a Java object.
	 */
	public static <T> T fromJson(final String inJson, Class<T> objClass) {
		try {
			boolean errorFound = false;
			// if API returned errors or warnings
			if (GsonErrors.class.isAssignableFrom(objClass)) {
				GsonErrors gsonErrors = gson.fromJson(inJson, GsonErrors.class);
				if (gsonErrors != null) {
					// print errors
					if (gsonErrors.getErrors() != null) {
						errorFound = true;
						Logging.error("The API returned the following error(s): ");
						for (String error : gsonErrors.getErrors()) {
							Logging.error(error);
						}
					}
					// print warnings
					if (gsonErrors.getWarnings() != null) {
						errorFound = true;
						Logging.warn("The API returned the following warning(s): ");
						for (String warning : gsonErrors.getWarnings()) {
							Logging.warn(warning);
						}
					}
					if (errorFound) return null;
				}
			}
			T resultObj = gson.fromJson(inJson, objClass);
			return resultObj;
		} catch (Exception e) {
			return null;
		}
	}

}
