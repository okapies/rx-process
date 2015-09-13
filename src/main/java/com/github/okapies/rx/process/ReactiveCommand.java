package com.github.okapies.rx.process;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;

import rx.Observer;

public class ReactiveCommand<T> extends AbstractReactiveProcessBuilder<T> {

    private final List<String> command;

    private final Map<String, String> environment;

    private final Path directory;

    private final ReactiveDecoder<T> decoder;

    private ReactiveCommand(
            List<String> command,
            Map<String, String> environment,
            Path directory,
            ReactiveDecoder<T> decoder) {
        this.command = command;
        this.environment = environment;
        this.directory = directory;
        this.decoder = decoder;
    }

    public static ReactiveCommand<ByteBuffer> build(String... command) {
        return build(Arrays.asList(command));
    }

    public static ReactiveCommand<ByteBuffer> build(List<String> command) {
        return new ReactiveCommand<>(
                command, new TreeMap<>(System.getenv()), null, RawDecoder.instance());
    }

    public List<String> command() {
        return this.command;
    }

    public ReactiveCommand<T> command(List<String> command) {
        return new ReactiveCommand<>(
                command, this.environment, this.directory, this.decoder);
    }

    public ReactiveCommand<T> command(String... command) {
        return new ReactiveCommand<>(
                Arrays.asList(command), this.environment, this.directory, this.decoder);
    }

    public Map<String, String> environment() {
        return this.environment;
    }

    public ReactiveCommand environment(Map<String, String> environment) {
        return new ReactiveCommand<>(
                this.command, environment, this.directory, this.decoder);
    }

    public Path directory() {
        return this.directory;
    }

    public ReactiveCommand<T> directory(String first, String... more) {
        Path path = Paths.get(first, more);
        return directory(path);
    }

    public ReactiveCommand<T> directory(URI uri) {
        Path path = Paths.get(uri);
        return directory(path);
    }

    public ReactiveCommand<T> directory(Path directory) {
        return new ReactiveCommand<>(this.command, this.environment, directory, this.decoder);
    }

    public ReactiveDecoder decoder() {
        return this.decoder;
    }

    public <R> ReactiveCommand<R> decoder(ReactiveDecoder<R> decoder) {
        return new ReactiveCommand<>(this.command, this.environment, this.directory, decoder);
    }

    @Override
    public ReactiveProcess run() {
        return run(null);
    }

    @Override
    public ReactiveProcess run(ProcessObserver<T> observer) {
        ReactiveProcessHandler<T> handler = ReactiveProcessHandler.create(decoder);

        // subscribe to stdout
        if (observer != null) {
            Observer<T> stdoutObserver = observer.stdout();
            if (stdoutObserver != null) {
                handler.decodedStdout().subscribe(stdoutObserver);
            }
        }

        // subscribe to stderr
        if (observer != null) {
            Observer<T> stderrObserver = observer.stderr();
            if (stderrObserver != null) {
                handler.decodedStderr().subscribe(stderrObserver);
            }
        }

        // run an observed process
        NuProcessBuilder builder = new NuProcessBuilder(handler, command);
        builder.setCwd(directory);
        builder.environment().clear();
        builder.environment().putAll(environment);
        NuProcess process = builder.start();

        return new DefaultReactiveProcess(process, handler.exitCode());
    }

}
