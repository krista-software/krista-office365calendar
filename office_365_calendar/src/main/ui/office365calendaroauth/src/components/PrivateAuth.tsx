import React, {useCallback, useEffect, useState} from "react";
import {AuthPayload, AuthType} from "./AuthPage";
import {getCredentials} from "../api";

const PrivateAuth = ({onAuthChange}: {
    onAuthChange: (authPayload: AuthPayload) => void
}) => {
    const [email, setEmail] = useState("");
    const [clientId, setClientId] = useState("");
    const [clientSecret, setClientSecret] = useState("");
    const [tenantId, setTenantId] = useState("");
    const [timeZone, setTimeZone] = useState("");
    const [savedCred, setSavedCred] = useState<boolean>(false);

    const areFieldsValid = useCallback(() => {
        return !!email && !!clientId && !!clientSecret && !!tenantId;
    }, [email, clientId, clientSecret, tenantId]);

    const isCredSaved = useCallback((): boolean => {
        return savedCred;
    }, [savedCred]);

    useEffect(() => {
        getAllCred().catch(error => console.log(error));
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    useEffect(() => {
        onAuthChange({
            authType: AuthType.Private,
            email: email,
            clientId: clientId,
            clientSecret: clientSecret,
            tenantId: tenantId,
            timeZone: timeZone,
            isAllRequiredFieldsHaveValue: areFieldsValid(),
            isCredentialsSaved: isCredSaved()
        });
    }, [email, clientId, clientSecret, tenantId, timeZone, onAuthChange, areFieldsValid, isCredSaved]);

    const getAllCred = async () => {
        try {
            const credentials = await getCredentials(AuthType.Private);
            if (credentials) {
                setEmail(credentials["email"]);
                setClientId(credentials["clientId"]);
                setClientSecret(credentials["clientSecret"]);
                setTenantId(credentials["tenantId"]);
                setTimeZone(credentials["timeZone"] || "");
                setSavedCred(true);
            } else {
                console.error('Failed to fetch credentials');
            }
        } catch (error) {
            console.error('Error fetching credentials:', error);
        }
    }

    return (
        <div>
            <div className="form-group">
                <label htmlFor="email">Email *</label>
                <input
                    type="email"
                    id="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Enter your email address"
                    required
                />
            </div>
            <div className="form-group">
                <label htmlFor="clientId">Client ID *</label>
                <input
                    type="text"
                    id="clientId"
                    value={clientId}
                    onChange={(e) => setClientId(e.target.value)}
                    placeholder="Enter your Azure App Client ID"
                    required
                />
            </div>
            <div className="form-group">
                <label htmlFor="clientSecret">Client Secret *</label>
                <input
                    type="password"
                    id="clientSecret"
                    value={clientSecret}
                    onChange={(e) => setClientSecret(e.target.value)}
                    placeholder="Enter your Azure App Client Secret"
                    required
                />
            </div>
            <div className="form-group">
                <label htmlFor="tenantId">Tenant ID *</label>
                <input
                    type="text"
                    id="tenantId"
                    value={tenantId}
                    onChange={(e) => setTenantId(e.target.value)}
                    placeholder="Enter your Azure Tenant ID"
                    required
                />
            </div>
            <div className="form-group">
                <label htmlFor="timeZone">Time Zone</label>
                <input
                    type="text"
                    id="timeZone"
                    value={timeZone}
                    onChange={(e) => setTimeZone(e.target.value)}
                    placeholder="Enter your time zone (optional)"
                />
            </div>
        </div>
    );
};

export default PrivateAuth;
