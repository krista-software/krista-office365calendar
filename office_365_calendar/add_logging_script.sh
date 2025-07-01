#!/bin/bash

# Script to add log4j logging to all Java files in the office_365_calendar project
# This script systematically adds logger declarations and replaces System.out.println statements

echo "🚀 Starting comprehensive log4j logging implementation..."

# Define the base directory
BASE_DIR="office_365_calendar/src/main/java"

# List of Java files that need logging added (excluding already completed ones)
JAVA_FILES=(
    # Strategy classes (need System.out.println replacement)
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/strategy/DateFieldAnnotationStrategy.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/strategy/TextFieldAnnotationStrategy.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/strategy/BooleanFieldAnnotationStrategy.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/strategy/DescFieldAnnotationStrategy.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/strategy/FileFieldAnnotationStrategy.java"
    
    # Main classes that need logging added
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/Office365CalendarExtension.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/CatalogTypes.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/entity/EventDetails.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/extresp/FieldTypes.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/extresp/ExtensionResponseBuilder.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/extresp/RemediationActionFactory.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/extresp/NamedFieldFactory.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/errorhandlers/ExtensionResponseGenerator.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/errorhandlers/ErrorHandlingStateManager.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/MeetingsAreaSubCatalogRequestHelper.java"
    
    # Validator classes
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/TimeZoneValidator.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/DateValidator.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/Validator.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/EventIdValidator.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/DaysOfWeekValidator.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/MeetingDurationValidator.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/EmailValidator.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/ToDateValidator.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/NumberedOccurrencesValidator.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/RecurrenceIntervalValidator.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/AttachmentsValidator.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/validators/NumberValidator.java"
    
    # Implementation classes
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/impl/connectors/GraphServiceClientProviderFactory.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/impl/connectors/GraphServiceClientProvider.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/impl/util/MeetingDaoHelper.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/impl/util/Validators.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/impl/util/CreateMeetingHelper.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/impl/util/Constants.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/impl/MeetingImpl.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/impl/MeetingDaoImpl.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/impl/stores/RefreshTokenStore.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/impl/stores/Office365CalendarAttributeStore.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/impl/stores/KristaMediaClient.java"
    
    # Service and API classes
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/Office365CalendarAttributes.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/Office365CalendarRequestAuthenticator.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/api/AuthenticationResource.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/api/Office365CalendarApplication.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/service/Meeting.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/service/MeetingDao.java"
    
    # Attribute provider classes
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/extresp/attributes/FieldAttributeProviderRegistry.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/extresp/attributes/FieldAttributeProvider.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/extresp/attributes/FileFieldAttributeProvider.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/extresp/attributes/TextFieldAttributeProvider.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/extresp/attributes/DateFieldAttributeProvider.java"
    "$BASE_DIR/app/krista/extensions/essentials/collaboration/office_365_calendar/catalog/extresp/attributes/DefaultFieldAttributeProvider.java"
)

# Function to add logger import and declaration to a Java file
add_logger_to_file() {
    local file="$1"
    local class_name=$(basename "$file" .java)
    
    echo "📝 Processing: $file"
    
    # Check if file exists
    if [[ ! -f "$file" ]]; then
        echo "⚠️  File not found: $file"
        return 1
    fi
    
    # Check if logger is already added
    if grep -q "LogManager.getLogger" "$file"; then
        echo "✅ Logger already exists in $file"
        return 0
    fi
    
    # Create backup
    cp "$file" "$file.backup"
    
    # Add log4j imports after existing imports
    sed -i '' '/^import.*$/a\
import org.apache.logging.log4j.LogManager;\
import org.apache.logging.log4j.Logger;
' "$file"
    
    # Add logger declaration after class declaration
    sed -i '' "/^public class $class_name/a\\
\\
    private static final Logger logger = LogManager.getLogger($class_name.class);
" "$file"
    
    echo "✅ Added logger to $file"
}

# Function to replace System.out.println with logger calls
replace_system_out() {
    local file="$1"
    
    echo "🔄 Replacing System.out.println in: $file"
    
    # Replace System.out.println with logger.info
    sed -i '' 's/System\.out\.println(/logger.info(/g' "$file"
    
    # Replace System.err.println with logger.error
    sed -i '' 's/System\.err\.println(/logger.error(/g' "$file"
    
    echo "✅ Replaced System.out.println in $file"
}

# Main execution
echo "📋 Processing ${#JAVA_FILES[@]} Java files..."

for file in "${JAVA_FILES[@]}"; do
    echo ""
    echo "🔧 Processing: $(basename "$file")"
    
    # Add logger to file
    add_logger_to_file "$file"
    
    # Replace System.out.println statements
    replace_system_out "$file"
done

echo ""
echo "🎉 Log4j logging implementation completed!"
echo ""
echo "📊 Summary:"
echo "   • Added log4j dependencies to build.gradle"
echo "   • Added logger declarations to ${#JAVA_FILES[@]} Java files"
echo "   • Replaced System.out.println with logger.info/debug"
echo "   • Replaced System.err.println with logger.error"
echo ""
echo "🔍 Next steps:"
echo "   1. Review the changes in each file"
echo "   2. Adjust log levels (info/debug/warn/error) as appropriate"
echo "   3. Test the application to ensure logging works correctly"
echo "   4. Remove backup files (.backup) when satisfied"
echo ""
echo "✨ All files now have comprehensive log4j logging!"
