/*
 * Office365Calendar Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.essentials.collaboration.office_365_calendar.catalog.extresp.attributes;

import java.util.Map;

/**
 * Interface for providing default attributes for different field types.
 * This interface follows the Strategy pattern to allow different field types
 * to have their own attribute configuration logic.
 */
public interface FieldAttributeProvider {
    
    /**
     * Gets the default attributes for this field type.
     *
     * @return a map containing the default attributes for the field type
     */
    Map<String, Object> getDefaultAttributes();
    
    /**
     * Gets the field type that this provider handles.
     *
     * @return the field type string (e.g., "text", "file", "date")
     */
    String getFieldType();
}
