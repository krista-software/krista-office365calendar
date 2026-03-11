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

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for managing field attribute providers.
 * This class follows the Strategy pattern and provides a centralized way to
 * access default attributes for different field types.
 */
public class FieldAttributeProviderRegistry {
    
    private static final Map<String, FieldAttributeProvider> providers = new HashMap<>();
    private static final FieldAttributeProvider defaultProvider = new DefaultFieldAttributeProvider();
    
    static {
        // Register all field attribute providers
        registerProvider(new TextFieldAttributeProvider());
        registerProvider(new FileFieldAttributeProvider());
        registerProvider(new DateFieldAttributeProvider());
        // Add more providers here as needed
    }
    
    /**
     * Registers a field attribute provider.
     *
     * @param provider the provider to register
     */
    private static void registerProvider(FieldAttributeProvider provider) {
        System.out.println("→ Registering field attribute provider for type: " + provider.getFieldType());
        providers.put(provider.getFieldType().toLowerCase(), provider);
    }
    
    /**
     * Gets the default attributes for the specified field type.
     *
     * @param fieldType the field type string (e.g., "Text", "File", "Date")
     * @return a map containing the default attributes for the field type
     */
    public static Map<String, Object> getDefaultAttributes(String fieldType) {
        System.out.println("→ Getting default attributes for field type: " + fieldType);
        
        FieldAttributeProvider provider = providers.get(fieldType.toLowerCase());
        if (provider != null) {
            System.out.println("  → Found specific provider for: " + fieldType);
            return provider.getDefaultAttributes();
        } else {
            System.out.println("  → Using default provider for: " + fieldType);
            return defaultProvider.getDefaultAttributes();
        }
    }
    
    /**
     * Checks if a specific provider exists for the given field type.
     *
     * @param fieldType the field type to check
     * @return true if a specific provider exists, false otherwise
     */
    public static boolean hasProvider(String fieldType) {
        return providers.containsKey(fieldType.toLowerCase());
    }
    
    /**
     * Gets all registered field types.
     *
     * @return a set of all registered field type names
     */
    public static java.util.Set<String> getRegisteredFieldTypes() {
        return providers.keySet();
    }
}
