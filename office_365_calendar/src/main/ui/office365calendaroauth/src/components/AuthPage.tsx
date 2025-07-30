import React, {useCallback, useEffect, useState} from "react";
import {saveCredentials, testConnection, getAuthKey} from "../api";
import Toast from "./Toast";
import PrivateAuth from "./PrivateAuth";
import PublicAuth from "./PublicAuth";

export interface AuthPayload {
    authType: AuthType;
    email: string;
    clientId?: string;
    clientSecret?: string;
    tenantId?: string;
    timeZone?: string;
    isAllRequiredFieldsHaveValue?: boolean;
    isCredentialsSaved?: boolean;
}

export enum AuthType {
    Public = "Public",
    Private = "Private"
}

const AuthPage = () => {
    const [selectedOption, setSelectedOption] = useState("Public");
    const [showToast, setShowToast] = useState<boolean>(false);
    const [loading, setLoading] = useState(false);
    const [toastMessage, setToastMessage] = useState<string>("");
    const [toastType, setToastType] = useState<"success" | "error" | "info">("error");
    const [authPayload, setAuthPayload] = useState<AuthPayload | null>(null);
    const [isConnectionSuccess, setIsConnectionSuccess] = useState<boolean>(false);

    useEffect(() => {
        getAuth().catch(error => console.log(error));
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const getAuth = async () => {
        try {
            const key = await getAuthKey();
            if (key !== selectedOption) {
                setSelectedOption(key as AuthType);
            }
        } catch (error) {
            console.error('Error fetching credentials:', error);
        }
    }

    const handleOptionChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedOption(e.target.value as AuthType);
    };

    const showNotification = (message: string, type: "success" | "error" | "info") => {
        setToastMessage(message);
        setToastType(type);
        setShowToast(true);
        setTimeout(() => {
            setShowToast(false);
        }, 3000);
    };

    const handleAuthChange = useCallback((payload: AuthPayload) => {
        setAuthPayload(payload);
    }, []);

    const handleTestConnectionClick = async () => {
        if (!authPayload) return;

        setLoading(true);
        try {
            const response = await testConnection(authPayload);
            if (response.success) {
                showNotification("Connection successful!", "success");
                setIsConnectionSuccess(true);
            } else {
                if (response.url) {
                    // Open authorization URL in new window
                    window.open(response.url, '_blank');
                    showNotification(response.errorMessage || "Authorization required. Please complete authentication in the new window.", "info");
                } else {
                    showNotification(response.errorMessage || "Connection failed", "error");
                }
                setIsConnectionSuccess(false);
            }
        } catch (error) {
            showNotification("Connection test failed", "error");
            setIsConnectionSuccess(false);
        } finally {
            setLoading(false);
        }
    };

    const handleSaveClick = async () => {
        if (!authPayload) return;

        setLoading(true);
        try {
            const response = await saveCredentials(authPayload);
            if (response.success) {
                showNotification("Credentials saved successfully!", "success");
            } else {
                showNotification(response.errorMessage || "Failed to save credentials", "error");
            }
        } catch (error) {
            showNotification("Failed to save credentials", "error");
        } finally {
            setLoading(false);
        }
    };

    const renderComponent = () => {
        switch (selectedOption) {
            case AuthType.Public:
                return <PublicAuth onAuthChange={handleAuthChange}/>;
            case AuthType.Private:
                return <PrivateAuth onAuthChange={handleAuthChange}/>;
            default:
                return <PublicAuth onAuthChange={handleAuthChange}/>;
        }
    };

    const setToastOff = () => {
        setShowToast(false);
    }

    const disableSaveButton = (): boolean => {
        return !isConnectionSuccess;
    }

    const checkMandatoryFields = (): boolean => {
        if (authPayload?.isAllRequiredFieldsHaveValue != null) {
            return !authPayload?.isAllRequiredFieldsHaveValue;
        }
        return true;
    }

    return (
        <>
            <div className="authOptions">
                <div>
                    <input
                        type="radio"
                        id="public"
                        value={AuthType.Public}
                        checked={selectedOption === AuthType.Public}
                        onChange={handleOptionChange}
                    />
                    <label htmlFor="public"></label>
                    Public
                </div>
                <div>
                    <input
                        type="radio"
                        id="private"
                        value={AuthType.Private}
                        checked={selectedOption === AuthType.Private}
                        onChange={handleOptionChange}
                    />
                    <label htmlFor="private"></label>
                    Private
                </div>
            </div>
            <div>
                {renderComponent()}
            </div>
            <div className="button-container">
                <button disabled={checkMandatoryFields() || loading}
                        className={`test-connection-button ${loading ? 'loading' : ''}`}
                        onClick={handleTestConnectionClick}
                >
                     <span className="button-content">
                         {loading && <div className="loading-spinner"></div>}
                         Test Connection
                     </span>
                </button>
                <button disabled={disableSaveButton() || loading}
                        className={`save-button ${loading ? 'loading' : ''}`}
                        onClick={handleSaveClick}
                >
                     <span className="button-content">
                         {loading && <div className="loading-spinner"></div>}
                         Save Changes
                     </span>
                </button>
            </div>
            {showToast && (
                <Toast
                    message={toastMessage}
                    type={toastType}
                    onClose={setToastOff}
                />
            )}
        </>
    );
};

export default AuthPage;
