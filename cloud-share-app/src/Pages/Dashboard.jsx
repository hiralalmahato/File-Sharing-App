import DashboardLayout from "../layout/DashboardLayout.jsx";
import {useContext, useEffect, useState} from "react";
import {UserCreditsContext} from "../Context/UserCreditsContext.jsx";
import axios from "axios";
import {apiEndpoints} from "../util/apiEndpoints.js";
import {Loader2} from "lucide-react";
import DashboardUpload from "../components/DashboardUpload.jsx";
import RecentFiles from "../components/RecentFiles.jsx";

const Dashboard = () => {
    const [files, setFiles] = useState([]);
    const [uploadFiles, setUploadFiles] = useState([]);
    const [uploading, setUploading] = useState(false);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [messageType, setMessageType] = useState('');
    const [dataError, setDataError] = useState('');
    const [remainingUploads, setRemainingUploads] = useState(5);
    const { fetchUserCredits } = useContext(UserCreditsContext);
    const MAX_FILES = 5;

    useEffect(() => {
        const fetchRecentFiles = async () => {
            setLoading(true);
            try {
                const res = await axios.get(apiEndpoints.FETCH_FILES);

                // Sort by uploadedAt and take only the 5 most recent files
                const sortedFiles = res.data.sort((a, b) =>
                    new Date(b.uploadedAt) - new Date(a.uploadedAt)
                ).slice(0, 5);
                setFiles(sortedFiles);
                setDataError('');
            } catch (error) {
                setFiles([]);
                setDataError('Unable to load data');
            } finally {
                setLoading(false);
            }
        };
        fetchRecentFiles();
    }, []);

    const handleFileChange = (e) => {
        const selectedFiles = Array.from(e.target.files);

        // Check if adding these files would exceed the limit
        if (uploadFiles.length + selectedFiles.length > MAX_FILES) {
            setMessage(`You can only upload a maximum of ${MAX_FILES} files at once.`);
            setMessageType('error');
            return;
        }

        // Add the new files to the existing files
        setUploadFiles(prevFiles => [...prevFiles, ...selectedFiles]);
        setMessage('');
        setMessageType('');
    };

    // Remove a file from the upload list
    const handleRemoveFile = (index) => {
        setUploadFiles(prevFiles => prevFiles.filter((_, i) => i !== index));
        setMessage('');
        setMessageType('');
    };

    // Calculate remaining uploads
    useEffect(() => {
        setRemainingUploads(MAX_FILES - uploadFiles.length);
    }, [uploadFiles]);

    // Handle file upload
    const handleUpload = async () => {
        if (uploadFiles.length === 0) {
            setMessage('Please select at least one file to upload.');
            setMessageType('error');
            return;
        }

        if (uploadFiles.length > MAX_FILES) {
            setMessage(`You can only upload a maximum of ${MAX_FILES} files at once.`);
            setMessageType('error');
            return;
        }

        setUploading(true);
        setMessage('Uploading files...');
        setMessageType('info');

        const formData = new FormData();
        uploadFiles.forEach(file => formData.append('files', file));

        try {
            await axios.post(apiEndpoints.UPLOAD_FILE, formData);

            setMessage('Files uploaded successfully!');
            setMessageType('success');
            setUploadFiles([]);

            // Refresh the recent files list
            const res = await axios.get(apiEndpoints.FETCH_FILES);

            // Sort by uploadedAt and take only the 5 most recent files
            const sortedFiles = res.data.sort((a, b) =>
                new Date(b.uploadedAt) - new Date(a.uploadedAt)
            ).slice(0, 5);

            setFiles(sortedFiles);
            setDataError('');

            // Refresh user credits immediately after successful upload
            await fetchUserCredits(true);
        } catch (error) {
            setMessage(error.response?.data?.message || 'Error uploading files. Please try again.');
            setMessageType('error');
        } finally {
            setUploading(false);
        }
    };

    return (
        <DashboardLayout activeMenu="Dashboard">
            <div className="p-6">
                <h1 className="text-2xl font-bold mb-6">My Drive</h1>
                <p className="text-gray-600 mb-6">Upload, manage, and share your files securely</p>
                {message && (
                    <div className={`mb-6 p-4 rounded-lg flex items-center gap-3 ${
                        messageType === 'error' ? 'bg-red-50 text-red-700' :
                            messageType === 'success' ? 'bg-green-50 text-green-700' :
                                'bg-purple-50 text-purple-700'
                    }`}>
                        {message}
                    </div>
                )}
                <div className="flex flex-col md:flex-row gap-6">
                    {/*Left column*/}
                    <div className="w-full md:w-[40%]">
                        <DashboardUpload
                            files={uploadFiles}
                            onFileChange={handleFileChange}
                            onUpload={handleUpload}
                            uploading={uploading}
                            onRemoveFile={handleRemoveFile}
                            remainingUploads={remainingUploads}
                        />
                    </div>

                    {/*right column*/}
                    <div className="w-full md:w-[60%]">
                        {dataError && (
                            <div className="bg-red-50 text-red-700 rounded-lg p-4 mb-4">{dataError}</div>
                        )}
                        {loading ? (
                            <div className="bg-white rounded-lg shadow p-8 flex flex-col items-center justify-center min-h-75">
                                <Loader2 size={40} className="text-purple-500 animate-spin mb-4" />
                                <p className="text-gray-500">Loading your files...</p>
                            </div>
                        ) : (
                            <RecentFiles files={files} />
                        )}
                    </div>
                </div>
            </div>
        </DashboardLayout>
    )
}

export default Dashboard;